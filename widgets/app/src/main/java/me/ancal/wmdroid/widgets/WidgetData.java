package me.ancal.wmdroid.widgets;

import android.content.*;
import android.os.BatteryManager;
import android.os.Build;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;
import org.xmlpull.v1.*;
import me.ancal.wmdroid.sdk.TodayContract;

/** Cached public display data. Credentials stay in private preferences and never enter rows/errors. */
public final class WidgetData {
    private WidgetData(){}
    public static final class Snapshot {
        public final String title,detail,source;public final long updated;public final boolean loading;
        public Snapshot(String title,String detail,String source,long updated,boolean loading){this.title=title;this.detail=detail;this.source=source;this.updated=updated;this.loading=loading;}
    }
    static final class DataFailure extends Exception {final String code;DataFailure(String code){super(code);this.code=code;}}
    private static final ExecutorService WORK=Executors.newFixedThreadPool(2);
    private static final Set<String> RUNNING=new HashSet<String>();
    private static final String[] KINDS={"weather","transit","splatnet","battery"};
    private static String title(Context context,String kind,String language){return "weather".equals(kind)?me.ancal.wmdroid.sdk.WmStrings.text(context,language,"날씨"):"transit".equals(kind)?me.ancal.wmdroid.sdk.WmStrings.text(context,language,"버스·지하철"):"splatnet".equals(kind)?me.ancal.wmdroid.sdk.WmStrings.text(context,language,"스플래툰 3 스테이지"):me.ancal.wmdroid.sdk.WmStrings.text(context,language,"배터리");}
    private static SharedPreferences cache(Context context){return context.getSharedPreferences("widget_cache",0);}
    private static final class Config {
        final String kind,provider,key,station,route,city,stationName,lat,lon,label,mode,signature;final boolean http;
        Config(Context context,String kind){SharedPreferences p=context.getSharedPreferences("widgets",0);this.kind=kind;provider=p.getString("transit_provider","gbis");key=p.getString("transit_key","").trim();station=p.getString("transit_station_id","").trim();route=p.getString("transit_route_id","").trim();city=p.getString("transit_city_code","").trim();stationName=p.getString("transit_station_name","시청").trim();http=p.getBoolean("transit_allow_http",false);lat=p.getString("weather_lat","37.5665").trim();lon=p.getString("weather_lon","126.9780").trim();label=p.getString("weather_label","서울").trim();mode=p.getString("splatnet_mode","regular");
            // All Splatoon modes share one downloaded schedule and one hourly request budget.
            signature=digest("weather".equals(kind)?lat+"\n"+lon:"transit".equals(kind)?provider+"\n"+key+"\n"+station+"\n"+route+"\n"+city+"\n"+stationName+"\n"+http:kind);
        }
        String problem(){if("weather".equals(kind)){try{double a=Double.parseDouble(lat),b=Double.parseDouble(lon);if(Double.isNaN(a)||Double.isInfinite(a)||Double.isNaN(b)||Double.isInfinite(b)||a< -90||a>90||b< -180||b>180)return "coordinates";}catch(Exception invalid){return "coordinates";}}
            if("transit".equals(kind)){if(key.isEmpty())return "key";if(!Arrays.asList("gbis","tago","seoul_bus","seoul_subway").contains(provider))return "settings";if("seoul_subway".equals(provider)){if(stationName.isEmpty())return "settings";}else if(station.isEmpty()||("seoul_bus".equals(provider)&&route.isEmpty())||("tago".equals(provider)&&city.isEmpty()))return "settings";if(provider.startsWith("seoul_")&&!http)return "http";}return "";
        }
        String source(Context context,String language){if("weather".equals(kind))return "Open-Meteo · https://open-meteo.com/";if("splatnet".equals(kind))return "Splatoon3.ink · https://splatoon3.ink/";if("battery".equals(kind))return "Android BatteryManager";return provider.startsWith("seoul_")?me.ancal.wmdroid.sdk.WmStrings.text(context,language,"서울 열린데이터광장")+" · https://data.seoul.go.kr/":"gbis".equals(provider)?me.ancal.wmdroid.sdk.WmStrings.text(context,language,"경기버스정보")+" · https://www.gbis.go.kr/":me.ancal.wmdroid.sdk.WmStrings.text(context,language,"국토교통부 TAGO")+" · https://www.data.go.kr/";}
    }
    public static Snapshot snapshot(Context context,String kind,String language){
        language=me.ancal.wmdroid.sdk.WmStrings.language(context,language);if(!Arrays.asList(KINDS).contains(kind))throw new IllegalArgumentException("Unknown widget");if("battery".equals(kind))return battery(context,language);
        Config config=new Config(context,kind);String problem=config.problem();if(!problem.isEmpty())return new Snapshot(title(context,kind,language),message(context,problem,language),config.source(context,language),0,false);
        Map<String,?> values=cache(context).getAll();String prefix=kind+".";boolean matches=config.signature.equals(string(values,prefix+"signature"));long updated=matches?number(values,prefix+"updated"):0;boolean loading;synchronized(RUNNING){loading=RUNNING.contains(kind);}
        String error=matches?string(values,prefix+"error"):"";
        if(updated>0){try{Snapshot parsed=parse(context,config,string(values,prefix+"body"),string(values,prefix+"locale"),language);String detail=parsed.detail;if(!error.isEmpty())detail+="\n"+me.ancal.wmdroid.sdk.WmStrings.text(context,language,"이전 정보 · ")+message(context,error,language);else if(System.currentTimeMillis()-updated>ttl(kind)*2)detail+="\n"+me.ancal.wmdroid.sdk.WmStrings.text(context,language,"이전 정보 · 갱신 대기");return new Snapshot(parsed.title,detail,config.source(context,language),updated,loading);}catch(Exception invalid){error="format";}}
        return new Snapshot(title(context,kind,language),message(context,error.isEmpty()?(loading?"loading":"waiting"):error,language),config.source(context,language),0,loading);
    }
    public static void refresh(Context context,String kind,boolean force){
        if(!Arrays.asList(KINDS).contains(kind))return;final Context app=context.getApplicationContext();if("battery".equals(kind)){if(force)notifyKind(app,kind);return;}
        final Config config=new Config(app,kind);if(!config.problem().isEmpty())return;final SharedPreferences p=cache(app);final String prefix=kind+".";long now=System.currentTimeMillis();
        synchronized(RUNNING){if(RUNNING.contains(kind))return;boolean matches=config.signature.equals(p.getString(prefix+"signature",""));long attempt=p.getLong(prefix+"attempt",0),updated=matches?p.getLong(prefix+"updated",0):0;
            if("splatnet".equals(kind)&&now>=attempt&&now-attempt<ttl(kind))return;
            if(matches&&!force&&!p.getString(prefix+"error","").isEmpty()&&now>=attempt&&now-attempt<60000)return;
            if(matches&&now>=attempt&&now-attempt<5000)return;if(!force&&updated>0&&now>=updated&&now-updated<ttl(kind))return;
            RUNNING.add(kind);p.edit().putLong(prefix+"attempt",now).apply();}
        WORK.execute(()->{String error="";try{
                String body=fetch(config),locale="";if("splatnet".equals(kind)){try{locale=get("https://splatoon3.ink/data/locale/ko-KR.json",false);}catch(Exception optional){locale=p.getString(prefix+"locale","");}}
                parse(app,config,body,locale,"ko");if(config.signature.equals(new Config(app,kind).signature))p.edit().putString(prefix+"signature",config.signature).putString(prefix+"body",body).putString(prefix+"locale",locale).putLong(prefix+"updated",System.currentTimeMillis()).remove(prefix+"error").apply();
            }catch(DataFailure failure){error=failure.code;}catch(javax.net.ssl.SSLException tls){error="tls";}catch(SocketTimeoutException timeout){error="timeout";}catch(Exception unavailable){error="network";}
            finally{p.edit().putLong(prefix+"attempt",System.currentTimeMillis()).apply();if(!error.isEmpty()&&config.signature.equals(new Config(app,kind).signature)){SharedPreferences.Editor edit=p.edit();if(!config.signature.equals(p.getString(prefix+"signature","")))edit.remove(prefix+"body").remove(prefix+"updated");edit.putString(prefix+"signature",config.signature).putString(prefix+"error",error).apply();}synchronized(RUNNING){RUNNING.remove(kind);}notifyKind(app,kind);}
        });
    }
    public static void notifyWidgets(Context context){for(String kind:KINDS)notifyKind(context,kind);}
    private static void notifyKind(Context context,String kind){context.getContentResolver().notifyChange(TodayContract.rows("me.ancal.wmdroid.widgets."+kind),null);}
    private static long ttl(String kind){return "splatnet".equals(kind)?3600000L:"weather".equals(kind)?900000L:60000L;}
    private static String digest(String value){try{byte[] bytes=MessageDigest.getInstance("SHA-256").digest(value.getBytes("UTF-8"));StringBuilder out=new StringBuilder();for(byte b:bytes)out.append(String.format(Locale.ROOT,"%02x",b&255));return out.toString();}catch(Exception impossible){throw new IllegalStateException(impossible);}}
    private static String encode(String value)throws Exception{return URLEncoder.encode(value,"UTF-8");}
    private static String fetch(Config c)throws Exception{
        if("weather".equals(c.kind))return get("https://api.open-meteo.com/v1/forecast?latitude="+encode(c.lat)+"&longitude="+encode(c.lon)+"&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=Asia%2FSeoul",false);
        if("splatnet".equals(c.kind))return get("https://splatoon3.ink/data/schedules.json",false);
        String key=encode(c.key);if("gbis".equals(c.provider))return get("https://apis.data.go.kr/6410000/busarrivalservice/v2/getBusArrivalListv2?format=json&serviceKey="+key+"&stationId="+encode(c.station),false);
        if("tago".equals(c.provider))return get("https://apis.data.go.kr/1613000/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList?serviceKey="+key+"&cityCode="+encode(c.city)+"&nodeId="+encode(c.station)+"&_type=json&numOfRows=100&pageNo=1",false);
        if("seoul_bus".equals(c.provider))return get("http://ws.bus.go.kr/api/rest/arrive/getArrInfoByRouteAll?serviceKey="+key+"&busRouteId="+encode(c.route),c.http);
        return get("http://swopenapi.seoul.go.kr/api/subway/"+key+"/json/realtimeStationArrival/0/20/"+encode(c.stationName),c.http);
    }
    private static String get(String address,boolean allowHttp)throws Exception{
        URL url=new URL(address);String host=url.getHost();if(!Arrays.asList("api.open-meteo.com","splatoon3.ink","apis.data.go.kr","ws.bus.go.kr","swopenapi.seoul.go.kr").contains(host))throw new DataFailure("network");
        boolean secure="https".equals(url.getProtocol());if(!secure&&(!allowHttp||!("ws.bus.go.kr".equals(host)||"swopenapi.seoul.go.kr".equals(host))))throw new DataFailure("http");if(secure&&Build.VERSION.SDK_INT<16)throw new DataFailure("tls");
        HttpURLConnection connection=(HttpURLConnection)url.openConnection();connection.setConnectTimeout(10000);connection.setReadTimeout(10000);connection.setInstanceFollowRedirects(false);connection.setRequestProperty("User-Agent","WMDroid/1.0 (+https://splatoon3.ink/)");connection.setRequestProperty("Accept","application/json, application/xml, text/xml");
        try{if(connection.getResponseCode()!=200)throw new DataFailure("service");try(InputStream in=connection.getInputStream();ByteArrayOutputStream bytes=new ByteArrayOutputStream()){byte[] buffer=new byte[8192];int count;while((count=in.read(buffer))!=-1){if(bytes.size()+count>2097152)throw new DataFailure("format");bytes.write(buffer,0,count);}return bytes.toString("UTF-8");}}finally{connection.disconnect();}
    }
    private static String string(Map<String,?> values,String key){Object value=values.get(key);return value instanceof String?(String)value:"";}
    private static long number(Map<String,?> values,String key){Object value=values.get(key);return value instanceof Number?((Number)value).longValue():0;}
    private static Snapshot parse(Context context,Config c,String body,String locale,String language)throws Exception{if("weather".equals(c.kind)){Snapshot value=parseWeather(context,body,language);return new Snapshot(c.label.isEmpty()?value.title:c.label+" · "+value.title,value.detail,value.source,value.updated,value.loading);}if("splatnet".equals(c.kind))return parseSplatnet(context,body,locale,System.currentTimeMillis(),c.mode,language);switch(c.provider){case "gbis":return parseGbis(context,body,c.route,language);case "tago":return parseTago(context,body,c.route,language);case "seoul_bus":return parseSeoulBus(context,body,c.station,c.route,language);default:return parseSeoulSubway(context,body,c.stationName,language);}}
    private static JSONObject json(String raw)throws DataFailure{try{return new JSONObject(raw);}catch(Exception invalid){throw new DataFailure("format");}}
    private static JSONObject object(JSONObject parent,String name){JSONObject child=parent.optJSONObject(name);return child==null?new JSONObject():child;}
    private static JSONArray array(Object value){if(value instanceof JSONArray)return (JSONArray)value;JSONArray a=new JSONArray();if(value instanceof JSONObject)a.put(value);return a;}
    private static Snapshot result(Context context,String kind,String language,String detail){return new Snapshot(title(context,kind,language),detail,"",System.currentTimeMillis(),false);}
    private static String empty(Context context,String language){return me.ancal.wmdroid.sdk.WmStrings.text(context,language,"현재 도착정보가 없습니다.");}
    private static String clean(String value){return value==null?"":value.replaceAll("[\\p{Cntrl}&&[^\\n]]","").trim();}
    private static void add(List<String> lines,String value){if(lines.size()<4&&!value.trim().isEmpty())lines.add(clean(value));}
    private static String join(Context context,List<String> lines,String language){if(lines.isEmpty())return empty(context,language);StringBuilder out=new StringBuilder();for(String line:lines){if(out.length()>0)out.append('\n');out.append(line);}return out.toString();}
    static Snapshot parseWeather(Context context,String raw,String language)throws Exception{
        JSONObject root=json(raw);if(root.optBoolean("error",false))throw new DataFailure("service");JSONObject current=root.optJSONObject("current");if(current==null||!current.has("temperature_2m"))throw new DataFailure("format");double degrees=current.optDouble("temperature_2m",Double.NaN);if(Double.isNaN(degrees)||Double.isInfinite(degrees))throw new DataFailure("format");
        int code=current.optInt("weather_code",-1);String conditions=code==0?me.ancal.wmdroid.sdk.WmStrings.text(context,language,"맑음"):code>=1&&code<=3?me.ancal.wmdroid.sdk.WmStrings.text(context,language,"구름"):code==45||code==48?me.ancal.wmdroid.sdk.WmStrings.text(context,language,"안개"):code>=71&&code<=77||code==85||code==86?me.ancal.wmdroid.sdk.WmStrings.text(context,language,"눈"):code>=95?me.ancal.wmdroid.sdk.WmStrings.text(context,language,"뇌우"):code>=51&&code<=82?me.ancal.wmdroid.sdk.WmStrings.text(context,language,"비"):me.ancal.wmdroid.sdk.WmStrings.text(context,language,"날씨 정보");
        String detail=String.format(Locale.ROOT,"%.1f°C · %s",degrees,conditions);if(current.has("relative_humidity_2m"))detail+="\n"+me.ancal.wmdroid.sdk.WmStrings.text(context,language,"습도 ")+current.optInt("relative_humidity_2m")+"%";if(current.has("wind_speed_10m"))detail+=" · "+me.ancal.wmdroid.sdk.WmStrings.text(context,language,"바람 ")+String.format(Locale.ROOT,"%.1f km/h",current.optDouble("wind_speed_10m",0));return result(context,"weather",language,detail);
    }
    static Snapshot parseGbis(Context context,String raw,String route,String language)throws Exception{
        JSONObject root=json(raw);JSONObject response=root.optJSONObject("response");if(response==null)response=root;JSONObject header=response.optJSONObject("msgHeader");if(header==null)throw new DataFailure("format");String code=header.optString("resultCode","");if("4".equals(code))return result(context,"transit",language,empty(context,language));if(!"0".equals(code))throw new DataFailure("service");JSONArray items=array(object(response,"msgBody").opt("busArrivalList"));List<String> lines=new ArrayList<String>();
        for(int i=0;i<items.length();i++){JSONObject item=items.optJSONObject(i);if(item==null||!route.isEmpty()&&!route.equals(item.optString("routeId")))continue;String number=item.optString("routeName",item.optString("routeId",""));int minutes=item.optInt("predictTime1",-1);String arrival=minutes>=0?minutes+me.ancal.wmdroid.sdk.WmStrings.text(context,language,"분","widgets.minutes_suffix"):me.ancal.wmdroid.sdk.WmStrings.text(context,language,"도착정보 대기");int stops=item.optInt("locationNo1",-1);if(stops>=0)arrival+=" · "+stops+me.ancal.wmdroid.sdk.WmStrings.text(context,language,"정류장 전");add(lines,number+" · "+arrival);}
        return result(context,"transit",language,join(context,lines,language));
    }
    static Snapshot parseTago(Context context,String raw,String route,String language)throws Exception{
        JSONObject root=json(raw),response=root.optJSONObject("response");if(response==null)throw new DataFailure("format");JSONObject header=response.optJSONObject("header");if(header==null)throw new DataFailure("format");String code=header.optString("resultCode","");if(!"00".equals(code)&&!"0".equals(code))throw new DataFailure("service");JSONArray items=array(object(object(response,"body"),"items").opt("item"));List<String> lines=new ArrayList<String>();for(int i=0;i<items.length();i++){JSONObject item=items.optJSONObject(i);if(item==null||!route.isEmpty()&&!route.equals(item.optString("routeid")))continue;int seconds=item.optInt("arrtime",-1),stops=item.optInt("arrprevstationcnt",-1);String arrival=seconds>=0?((seconds+59)/60)+me.ancal.wmdroid.sdk.WmStrings.text(context,language,"분","widgets.minutes_suffix"):me.ancal.wmdroid.sdk.WmStrings.text(context,language,"도착정보 대기");if(stops>=0)arrival+=" · "+stops+me.ancal.wmdroid.sdk.WmStrings.text(context,language,"정류장 전");add(lines,item.optString("routeno",item.optString("routeid",""))+" · "+arrival);}return result(context,"transit",language,join(context,lines,language));
    }
    static Snapshot parseSeoulBus(Context context,String raw,String station,String route,String language)throws Exception{
        if(raw.toUpperCase(Locale.ROOT).contains("<!DOCTYPE")||raw.toUpperCase(Locale.ROOT).contains("<!ENTITY"))throw new DataFailure("format");List<Map<String,String>> items=new ArrayList<Map<String,String>>();Map<String,String> item=null;String code="",tag="";
        try{XmlPullParser parser=android.util.Xml.newPullParser();parser.setInput(new StringReader(raw));for(int event=parser.getEventType();event!=XmlPullParser.END_DOCUMENT;event=parser.next()){if(event==XmlPullParser.START_TAG){tag=parser.getName();if("itemList".equals(tag))item=new HashMap<String,String>();}else if(event==XmlPullParser.TEXT){String value=parser.getText();if("headerCd".equals(tag))code=value.trim();else if(item!=null)item.put(tag,(item.containsKey(tag)?item.get(tag):"")+value);}else if(event==XmlPullParser.END_TAG){if("itemList".equals(parser.getName())&&item!=null){items.add(item);item=null;}tag="";}}}catch(Exception invalid){throw new DataFailure("format");}
        if(code.isEmpty())throw new DataFailure("format");if("4".equals(code))return result(context,"transit",language,empty(context,language));if(!"0".equals(code))throw new DataFailure("service");List<String> lines=new ArrayList<String>();for(Map<String,String> value:items){if(!station.equals(value.get("stId"))&&!station.equals(value.get("arsId")))continue;String id=value.get("busRouteId");if(id!=null&&!id.isEmpty()&&!route.isEmpty()&&!route.equals(id))continue;String name=value.get("rtNm");if(name==null)name=route;String first=value.get("arrmsg1"),second=value.get("arrmsg2");if(first!=null)add(lines,name+" · "+first);if(second!=null&&!second.equals(first))add(lines,name+" · "+second);}return result(context,"transit",language,join(context,lines,language));
    }
    static Snapshot parseSeoulSubway(Context context,String raw,String station,String language)throws Exception{
        JSONObject root=json(raw);JSONObject error=root.optJSONObject("errorMessage");if(error!=null){String code=error.optString("code","");if("INFO-200".equals(code))return result(context,"transit",language,empty(context,language));if(!"INFO-000".equals(code)&&!"0".equals(code))throw new DataFailure("service");}else if(root.has("RESULT")){throw new DataFailure("service");}else if(!root.has("realtimeArrivalList"))throw new DataFailure("format");
        JSONArray items=array(root.opt("realtimeArrivalList"));List<String> lines=new ArrayList<String>();for(int i=0;i<items.length();i++){JSONObject item=items.optJSONObject(i);if(item==null||!station.equals(item.optString("statnNm")))continue;add(lines,item.optString("trainLineNm",item.optString("bstatnNm",""))+" · "+item.optString("arvlMsg2",me.ancal.wmdroid.sdk.WmStrings.text(context,language,"도착정보 대기")));}return result(context,"transit",language,join(context,lines,language));
    }
    private static long instant(String value)throws DataFailure{try{SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);format.setLenient(false);format.setTimeZone(TimeZone.getTimeZone("UTC"));return format.parse(value).getTime();}catch(Exception invalid){throw new DataFailure("format");}}
    static Snapshot parseSplatnet(Context context,String raw,String localeRaw,long now,String mode,String language)throws Exception{
        JSONObject root=json(raw);JSONArray errors=root.optJSONArray("errors");if(errors!=null&&errors.length()>0)throw new DataFailure("service");JSONObject data=root.optJSONObject("data");if(data==null)throw new DataFailure("format");String schedules="regular".equals(mode)?"regularSchedules":"x".equals(mode)?"xSchedules":"bankaraSchedules";JSONObject schedule=data.optJSONObject(schedules);if(schedule==null)throw new DataFailure("format");JSONArray nodes=schedule.optJSONArray("nodes");if(nodes==null)throw new DataFailure("format");JSONObject current=null;long begin=0,end=0;
        for(int i=0;i<nodes.length();i++){JSONObject node=nodes.optJSONObject(i);if(node==null)continue;long start=instant(node.optString("startTime")),finish=instant(node.optString("endTime"));if(start<=now&&now<finish){current=node;begin=start;end=finish;break;}}
        if(current==null)return result(context,"splatnet",language,me.ancal.wmdroid.sdk.WmStrings.text(context,language,"현재 시간의 공개 스케줄이 없습니다."));
        JSONObject setting;if("regular".equals(mode))setting=current.optJSONObject("regularMatchSetting");else if("x".equals(mode))setting=current.optJSONObject("xMatchSetting");else{setting=null;JSONArray all=current.optJSONArray("bankaraMatchSettings");if(all!=null)for(int i=0;i<all.length();i++){JSONObject candidate=all.optJSONObject(i);if(candidate!=null&&("bankara_open".equals(mode)?"OPEN":"CHALLENGE").equals(candidate.optString("bankaraMode"))){setting=candidate;break;}}}
        if(setting==null)return result(context,"splatnet",language,me.ancal.wmdroid.sdk.WmStrings.text(context,language,"현재 선택한 모드가 열려 있지 않습니다."));
        JSONObject locale=new JSONObject();if("ko".equals(language)&&localeRaw!=null&&!localeRaw.isEmpty()){try{locale=json(localeRaw);}catch(DataFailure unavailable){/* English source names remain a truthful fallback. */}}
        JSONObject rule=object(setting,"vsRule");String ruleName=localized(locale,"rules",rule);JSONArray stages=setting.optJSONArray("vsStages");if(stages==null||stages.length()==0)throw new DataFailure("format");List<String> names=new ArrayList<String>();for(int i=0;i<stages.length();i++){JSONObject stage=stages.optJSONObject(i);if(stage!=null)names.add(localized(locale,"stages",stage));}
        String modeName="regular".equals(mode)?me.ancal.wmdroid.sdk.WmStrings.text(context,language,"레귤러 매치"):"x".equals(mode)?me.ancal.wmdroid.sdk.WmStrings.text(context,language,"X 매치"):"bankara_open".equals(mode)?me.ancal.wmdroid.sdk.WmStrings.text(context,language,"카오폴리스 매치 오픈"):me.ancal.wmdroid.sdk.WmStrings.text(context,language,"카오폴리스 매치 챌린지");StringBuilder detail=new StringBuilder(modeName);if(!ruleName.isEmpty())detail.append(" · ").append(ruleName);for(String name:names)detail.append('\n').append(name);SimpleDateFormat time=new SimpleDateFormat("MM/dd HH:mm",Locale.ROOT);detail.append('\n').append(time.format(new Date(begin))).append(" – ").append(time.format(new Date(end)));return result(context,"splatnet",language,detail.toString());
    }
    private static String localized(JSONObject locale,String group,JSONObject value){String name=value.optString("name","");JSONObject entry=object(locale,group).optJSONObject(value.optString("id",""));return entry==null?clean(name):clean(entry.optString("name",name));}
    private static Snapshot battery(Context context,String language){Intent state=context.registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));if(state==null)return new Snapshot(title(context,"battery",language),me.ancal.wmdroid.sdk.WmStrings.text(context,language,"배터리 정보를 읽을 수 없습니다."),"Android BatteryManager",0,false);int level=state.getIntExtra(BatteryManager.EXTRA_LEVEL,-1),scale=state.getIntExtra(BatteryManager.EXTRA_SCALE,100),status=state.getIntExtra(BatteryManager.EXTRA_STATUS,-1);String charge=status==BatteryManager.BATTERY_STATUS_FULL?me.ancal.wmdroid.sdk.WmStrings.text(context,language,"충전 완료","widgets.battery.full"):status==BatteryManager.BATTERY_STATUS_CHARGING?me.ancal.wmdroid.sdk.WmStrings.text(context,language,"충전 중"):me.ancal.wmdroid.sdk.WmStrings.text(context,language,"배터리 사용 중","widgets.battery.on_battery");String detail=(level>=0&&scale>0?Math.round(level*100f/scale)+"%":"—")+" · "+charge;if(state.hasExtra(BatteryManager.EXTRA_TEMPERATURE))detail+="\n"+me.ancal.wmdroid.sdk.WmStrings.text(context,language,"온도 ")+String.format(Locale.ROOT,"%.1f°C",state.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0)/10f);return new Snapshot(title(context,"battery",language),detail,"Android BatteryManager",System.currentTimeMillis(),false);}
    private static String message(Context context,String reason,String language){switch(reason){
        case "key":return me.ancal.wmdroid.sdk.WmStrings.text(context,language,"앱 설정에서 교통 API 인증키를 입력하세요.");
        case "settings":return me.ancal.wmdroid.sdk.WmStrings.text(context,language,"정류소·역·노선 설정을 확인하세요.");
        case "coordinates":return me.ancal.wmdroid.sdk.WmStrings.text(context,language,"올바른 위도·경도를 입력하세요.");
        case "http":return me.ancal.wmdroid.sdk.WmStrings.text(context,language,"서울 API를 사용하려면 설정에서 HTTP 연결을 허용하세요.");
        case "tls":return me.ancal.wmdroid.sdk.WmStrings.text(context,language,"이 기기의 TLS 연결을 사용할 수 없습니다. 최신 Android에서 다시 시도하세요.");
        case "timeout":return me.ancal.wmdroid.sdk.WmStrings.text(context,language,"서버 응답 시간이 초과되었습니다.");
        case "service":return me.ancal.wmdroid.sdk.WmStrings.text(context,language,"서비스 응답 오류입니다. 인증키·호출 한도·서비스 상태를 확인하세요.");
        case "format":return me.ancal.wmdroid.sdk.WmStrings.text(context,language,"제공자의 응답을 읽을 수 없습니다.");
        case "network":return me.ancal.wmdroid.sdk.WmStrings.text(context,language,"네트워크 연결을 확인하세요.");
        case "loading":return me.ancal.wmdroid.sdk.WmStrings.text(context,language,"불러오는 중…");
        default:return me.ancal.wmdroid.sdk.WmStrings.text(context,language,"새로 고침을 눌러 정보를 불러오세요.");
    }}
}
