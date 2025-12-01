(function () {
    'use strict';

    /* ===== 유틸 ===== */
    function q(id){ return document.getElementById(id); }
    function setText(id, v){ const el=q(id); if(el) el.textContent = (v ?? '-'); }
    function getCsrf(){
        const t=document.querySelector('meta[name="_csrf"]');
        const h=document.querySelector('meta[name="_csrf_header"]');
        return (t&&h)?{token:t.content, header:h.content}:null;
    }
    function toast(msg, type){
        let box=document.getElementById('toastBox');
        if(!box){ box=document.createElement('div'); box.id='toastBox';
            box.style.cssText='position:fixed;right:16px;bottom:16px;z-index:9999'; document.body.appendChild(box); }
        const el=document.createElement('div');
        el.className='alert alert-'+(type||'success')+' shadow-sm py-2 px-3 mb-2';
        el.textContent=msg; box.appendChild(el);
        setTimeout(()=>{ el.style.transition='opacity .2s'; el.style.opacity='0'; setTimeout(()=>el.remove(),200); },1800);
    }

    /* ===== 페이지 컨텍스트 ===== */
    const body = document.body;
    const role = (body.getAttribute('data-role') || 'STORE').toUpperCase();
    const itemNo = body.getAttribute('data-item-no');

    if (!itemNo) return;

    /* ===== 버튼 표시 제어 (서버 템플릿 th:if가 기본이지만, 클라이언트 보정도) ===== */
    function toggleAdminButtons() {
        const show = (role === 'MANAGER');
        ['btnEdit','btnDelete','btnAddNew'].forEach(id=>{
            const el = q(id); if(el){ el.style.display = show ? '' : 'none'; }
        });
    }
    toggleAdminButtons();

    /* ===== 데이터 로드 ===== */
    fetch('/api/items/' + itemNo)
        .then(res => { if(!res.ok) throw new Error(); return res.json(); })
        .then(item => {
            setText('d-category', item.itemCategory);
            setText('d-itemCode', item.itemCode);
            setText('d-itemName', item.itemName);
            setText('d-ingredientName', item.ingredientName);

            setText('d-stockUnit', item.stockUnit);
            setText('d-supplyUnit', item.supplyUnit);
            setText('d-convert', (item.convertStock != null ? item.convertStock.toLocaleString() : '-') + (item.stockUnit ? ' ' + item.stockUnit : ''));

            setText('d-itemPrice', (item.itemPrice != null ? item.itemPrice.toLocaleString() + ' 원' : '-'));
            setText('d-supplier', item.supplier);

            setText('d-storageType', item.storageType);
            setText('d-expType', item.expirationType);
            setText('d-expiration', (item.expiration != null ? item.expiration + ' 일' : '-'));

            setText('d-note', item.note);
            const img = document.getElementById('d-itemImage');
            if (img && item.itemImage) img.src = item.itemImage;
        })
        .catch(()=> toast('품목 정보를 불러오지 못했습니다.', 'danger'));

    /* ===== 버튼 액션 ===== */
    const btnEdit = q('btnEdit');
    if (btnEdit) btnEdit.addEventListener('click', ()=> location.href = '/item/set?itemNo='+itemNo);

    const btnDelete = q('btnDelete');
    if (btnDelete) btnDelete.addEventListener('click', ()=>{
        if (role !== 'MANAGER') return;
        if (!confirm('정말 삭제(소프트 삭제)하시겠습니까?')) return;

        const csrf = getCsrf();
        const headers = { 'Content-Type':'application/json' };
        if (csrf) headers[csrf.header] = csrf.token;

        fetch('/api/items/'+itemNo, { method:'DELETE', headers, credentials:'same-origin' })
            .then(res => { if(!res.ok) throw new Error(); toast('삭제되었습니다.','success'); setTimeout(()=>location.href='/item/get', 400); })
            .catch(()=> toast('삭제에 실패했습니다.','danger'));
    });

    const btnAddNew = q('btnAddNew');
    if (btnAddNew) btnAddNew.addEventListener('click', ()=> location.href = '/item/add');

})();
