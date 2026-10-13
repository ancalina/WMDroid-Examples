#!/usr/bin/env python3
"""Generate the example's API descriptions from the matching SDK source tree."""
import argparse,json,re
from pathlib import Path

def catalog(source):
    result=[]
    for path in sorted(source.glob('*.java')):
        text=path.read_text()
        if not re.search(r'public\s+(?:(?:abstract|final)\s+)*(?:class|interface|enum)\s+'+re.escape(path.stem)+r'\b',text): continue
        match=re.search(r'/\*\*(.*?)\*/\s*public\s+(?:(?:abstract|final)\s+)*(?:class|interface|enum)\s+'+re.escape(path.stem)+r'\b',text,re.S)
        if not match: raise ValueError('Missing public type documentation: '+path.name)
        doc='\n'.join(re.sub(r'^\s*\* ?', '', line).strip() for line in match[1].splitlines()).strip()
        result.append({'name':path.stem,'description':doc})
    assert len(result)>100 and len({row['name'] for row in result})==len(result)
    return result

if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('sdk_source',type=Path);args=p.parse_args()
    output=Path(__file__).resolve().parents[1]/'sample/app/src/main/assets/sdk-api-catalog.json'
    output.write_text(json.dumps(catalog(args.sdk_source),ensure_ascii=False,indent=2)+'\n')
    print(f'{output.name}: {len(json.loads(output.read_text()))} public SDK types')
