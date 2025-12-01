function fetchUtil(url, action, method="GET", json=null){
    const options = {
        method: method
    };

    if(method !== "GET" && json !== null){
        options.headers = {
            "Content-Type" : "application/json"
        };

        if(typeof json === "object"){
            options.body = JSON.stringify(json);
        }else{
            options.body = json;
        }
    }

    fetch(url, options)
    .then(data => data.json())
    .then(action)
    .catch(err => console.error(err));
}

$(document).on("click", "#pagination .page-link", function () {

    const page = $(this).data("page");
    const current = $("#pagination .active a").data("page");

    // 이전
    if (page === "prev") {
        if (current > 1) loadPage(current - 1);
        return;
    }

    // 다음
    if (page === "next") {
        loadPage(current + 1);
        return;
    }

    // 숫자
    if (!isNaN(page)) {
        loadPage(page);
    }
});


function updatePaginationUI(currentPage, totalPages) {

    const pag = $("#pagination");
    pag.empty();

    const blockSize = 5;                        // 한 번에 보여줄 페이지 수
    const currentBlock = Math.floor((currentPage - 1) / blockSize);
    const startPage = currentBlock * blockSize + 1;
    const endPage = Math.min(startPage + blockSize - 1, totalPages);

    // -------------------------
    // 이전 버튼
    // -------------------------
    pag.append(`
        <li class="page-item ${currentPage === 1 ? "disabled" : ""}">
            <a class="page-link" data-page="prev">이전</a>
        </li>
    `);

    // -------------------------
    // 페이지 번호(5개씩)
    // -------------------------
    for (let i = startPage; i <= endPage; i++) {
        pag.append(`
            <li class="page-item ${i === currentPage ? "active" : ""}">
                <a class="page-link" data-page="${i}">${i}</a>
            </li>
        `);
    }

    // -------------------------
    // 다음 버튼
    // -------------------------
    pag.append(`
        <li class="page-item ${currentPage === totalPages ? "disabled" : ""}">
            <a class="page-link" data-page="next">다음</a>
        </li>
    `);
}
