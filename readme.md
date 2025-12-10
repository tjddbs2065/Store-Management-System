# Branch Strategy & Commit Convention

## 1. Branch 구조

### main
- 배포용 브랜치  
- 항상 안정된 코드만 존재  
- 직접 커밋 금지  
- develop → main merge만 허용  

### develop
- 기능 통합 브랜치  
- feature / fix / refactor 브랜치에서 작업 완료 후 merge  
- 배포 전 최종 통합 코드 유지  

### feature/*
- 기능 단위 개발 브랜치  
- develop에서 분기  
- UI, Controller, Service, Repository 등 기능 전체 코드 포함  
- 테스트 코드도 feature 브랜치 내부에서 작성  
- 기능 완료 후 develop으로 PR → merge 후 feature 브랜치 삭제  

### fix/*
- 버그 수정 전용 브랜치  
- develop에서 분기  
- 버그 해결 후 develop에 merge → 브랜치 삭제  

### refactor/*
- 기능 변화 없이 코드 구조 개선 전용 브랜치  
- develop에 merge 후 삭제  

### docs/*
- 문서 변경 전용 브랜치  
- develop으로 merge 후 삭제  


---

## 2. Branch Workflow

feature/*
fix/*
refactor/*
→ develop → main(배포)


- 기능 개발 → develop 통합 → main 배포  


---

## 3. Commit Convention

형식:  
`type: description`  
영어, 명령형 사용 (예: add, fix, update…)

### Commit Types

- **feat** : 새로운 기능 추가  
  - 예) `feat: add sales chart`

- **fix** : 버그 수정  
  - 예) `fix: correct stock amount calc`

- **style** : UI/스타일 변경 (기능 영향 없음)  
  - 예) `style: update table layout`

- **refactor** : 코드 구조 개선 (기능 변화 없음)  
  - 예) `refactor: simplify service logic`

- **docs** : 문서 수정  
  - 예) `docs: update ERD schema`

- **test** : 테스트 코드 추가/수정 (feature 브랜치 내부에서만 사용)  
  - 예) `test: add item repository test`

- **delete** : 불필요 파일/코드 삭제  
  - 예) `delete: remove unused dto`

- **revert** : 이전 커밋 되돌림  
  - 예) `revert: undo wrong calculation`

- **wip** : 작업중 임시 커밋  
  - 예) `wip: implement filter function`

- **merge** : 브랜치 병합 커밋  
  - 예) `merge: feature/menu into develop`
