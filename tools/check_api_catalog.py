#!/usr/bin/env python3
"""Check that the UI catalog covers every public top-level type in the supplied AAR."""
import argparse,io,json,struct,zipfile
from pathlib import Path

def public_class(data):
    assert data[:4]==b'\xca\xfe\xba\xbe'
    count=struct.unpack_from('>H',data,8)[0];offset=10;index=1
    sizes={3:4,4:4,5:8,6:8,7:2,8:2,9:4,10:4,11:4,12:4,15:3,16:2,17:4,18:4,19:2,20:2}
    while index<count:
        tag=data[offset];offset+=1
        if tag==1:
            size=struct.unpack_from('>H',data,offset)[0];offset+=2+size
        else:
            assert tag in sizes,tag
            offset+=sizes[tag]
            if tag in (5,6):index+=1
        index+=1
    return bool(struct.unpack_from('>H',data,offset)[0]&1)

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('sdk_aar',type=Path);args=parser.parse_args()
    root=Path(__file__).resolve().parents[1]
    types=json.loads((root/'sample/app/src/main/assets/sdk-api-catalog.json').read_text())
    names={row['name'] for row in types}
    assert len(names)==len(types) and all(row['description'].strip() for row in types)
    with zipfile.ZipFile(args.sdk_aar) as aar,zipfile.ZipFile(io.BytesIO(aar.read('classes.jar'))) as jar:
        public={Path(name).stem for name in jar.namelist() if name.startswith('me/ancal/wmdroid/sdk/') and name.endswith('.class') and '$' not in name and public_class(jar.read(name))}
    assert names==public, {'missing':sorted(public-names),'extra':sorted(names-public)}
    print(f'PASS {len(names)} public SDK types, descriptions, matching AAR')
