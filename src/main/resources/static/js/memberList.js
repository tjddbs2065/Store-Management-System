(function ($) {
    'use strict';

    // 현재 활성 탭: 'manager' | 'store'
    let currentTab = 'manager';

    // -----------------------------
    // 테이블 렌더링
    // -----------------------------
    function renderManagerTable(list) {
        const $tbody = $('#managerTbody');
        $tbody.empty();

        if (!list || list.length === 0) {
            $tbody.append(
                '<tr><td colspan="5" class="text-center">등록된 본사 직원이 없습니다.</td></tr>'
            );
            return;
        }

        list.forEach(function (m) {
            const managerName = m.managerName || '';
            const managerId = m.managerId || '';
            const email = m.email || '';
            const phoneNumber = m.phoneNumber || '';

            const rowHtml = `
                <tr>
                    <td>${managerName}</td>
                    <td>${managerId}</td>
                    <td>${email}</td>
                    <td>${phoneNumber}</td>
                    <td>
                        <!-- 아직 기능 미구현: 버튼만 노출 -->
                        <button type="button"
                                class="btn btn-sm btn-outline-secondary managerSetbtn"
                                disabled>
                            수정
                        </button>
                    </td>
                </tr>
            `;
            $tbody.append(rowHtml);
        });
    }

    function renderStoreTable(list) {
        const $tbody = $('#storeTbody');
        $tbody.empty();

        if (!list || list.length === 0) {
            $tbody.append(
                '<tr><td colspan="6" class="text-center">등록된 직영점 직원이 없습니다.</td></tr>'
            );
            return;
        }

        list.forEach(function (s) {
            const storeNo = s.storeNo || '';
            const storeName = s.storeName || '';
            const storeManagerId = s.storeManagerId || '';
            const email = s.email || '';
            const storePhoneNumber = s.storePhoneNumber || '';
            const menuStopRole = s.menuStopRole || 'N';

            const checkedAttr = (menuStopRole === 'Y') ? 'checked' : '';

            const rowHtml = `
                <tr>
                    <td>${storeName}</td>
                    <td>${storeManagerId}</td>
                    <td>${email}</td>
                    <td>${storePhoneNumber}</td>
                    <td>
                        <div class="form-check form-switch d-flex justify-content-center">
                            <!-- 메뉴 판매 중지 권한 토글 -->
                            <input class="form-check-input store-menu-toggle"
                                   type="checkbox"
                                   ${checkedAttr}
                                   data-store-no="${storeNo}">
                        </div>
                    </td>
                    <td>
                        <!-- 상세보기 버튼: 아이콘만, 테두리/배경 없음 (기능 미구현) -->
                        <button type="button"
                                class="btn p-0 border-0 bg-transparent detailStoreBtn"
                                disabled>
                            <i class="bi bi-file-earmark-text" style="font-size: 1.6rem;"></i>
                        </button>
                    </td>
                </tr>
            `;
            $tbody.append(rowHtml);
        });
    }

    // -----------------------------
    // 서버 호출 (목록)
    // -----------------------------
    function fetchManagerPage(page) {
        const zeroBased = page - 1;
        const url = `/admin/member/manager?page=${zeroBased}`;

        fetchUtil(url, function (data) {
            const content = data && data.content ? data.content : [];
            const current = (typeof data.number === 'number') ? (data.number + 1) : page;
            const total = (typeof data.totalPages === 'number') ? data.totalPages : 1;

            renderManagerTable(content);
            updatePaginationUI(current, total);
        });
    }

    function fetchStorePage(page) {
        const zeroBased = page - 1;
        const url = `/admin/member/store?page=${zeroBased}`;

        fetchUtil(url, function (data) {
            const content = data && data.content ? data.content : [];
            const current = (typeof data.number === 'number') ? (data.number + 1) : page;
            const total = (typeof data.totalPages === 'number') ? data.totalPages : 1;

            renderStoreTable(content);
            updatePaginationUI(current, total);
        });
    }

    // -----------------------------
    // util.js 가 호출하는 전역 함수
    // -----------------------------
    window.loadPage = function (page) {
        if (page < 1) {
            page = 1;
        }

        // util.js 의 전역 currentPage 사용 (재선언 X)
        currentPage = page;

        if (currentTab === 'store') {
            fetchStorePage(page);
        } else {
            fetchManagerPage(page);
        }
    };

    // -----------------------------
    // 초기 바인딩
    // -----------------------------
    $(function () {

        // 탭 전환
        $('#tabManager').on('click', function () {
            if (currentTab === 'manager') return;

            currentTab = 'manager';

            $('#tabManager').addClass('active');
            $('#tabStore').removeClass('active');

            $('#sectionManager').addClass('active');
            $('#sectionStore').removeClass('active');

            loadPage(1);
        });

        $('#tabStore').on('click', function () {
            if (currentTab === 'store') return;

            currentTab = 'store';

            $('#tabStore').addClass('active');
            $('#tabManager').removeClass('active');

            $('#sectionStore').addClass('active');
            $('#sectionManager').removeClass('active');

            loadPage(1);
        });

        // 계정 등록 버튼: 안내만
        $('#addMemberBtn').on('click', function () {
            alert('계정 등록 기능은 추후 구현 예정입니다.');
        });

        // 직영점 - 메뉴 판매 중지 권한 토글 이벤트
        $('#storeTbody').on('change', '.store-menu-toggle', function () {
            const $cb = $(this);
            const storeNo = $cb.data('store-no');
            if (!storeNo) return;

            const isChecked = $cb.is(':checked');
            const newRole = isChecked ? 'Y' : 'N';

            const payload = {
                storeNo: storeNo,
                menuStopRole: newRole
            };

            // /admin/member/store/menuStopRole 로 PATCH
            fetchUtil('/admin/member/store/menuStopRole', function (res) {
                console.log('menuStopRole updated:', res);
                // 필요하면 여기서 toast 띄우기
            }, 'POST', payload);
        });

        // 첫 로딩: 본사 직원 1페이지
        loadPage(1);
    });

})(jQuery);
