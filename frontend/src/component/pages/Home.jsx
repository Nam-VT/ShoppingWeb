import React, {useEffect, useState} from "react";
import { useLocation } from "react-router-dom";
import ProductList from "../common/ProductList";
import Pagination from "../common/Pagination";
import ApiService from "../../service/ApiService";
import '../../style/home.css';

const itemsPerPage = 10;

const getInitialSmartSearch = (location) => {
    // Ưu tiên lấy từ location.state, nếu không thì lấy từ localStorage
    if (location.state?.searchResult) return location.state.searchResult;
    const last = localStorage.getItem('lastSmartSearch');
    if (last) {
        try {
            return JSON.parse(last).searchResult;
        } catch {}
    }
    return null;
};

const Home = () => {
    const location = useLocation();
    const [products, setProducts] = useState([]);
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(0);
    const [error, setError] = useState(null);
    const [searchResult, setSearchResult] = useState(() => getInitialSmartSearch(location));

    // Khi bấm Home, location.state sẽ mất, cần xóa smart search khỏi localStorage và state
    useEffect(() => {
        if (!location.state?.searchResult) {
            localStorage.removeItem('lastSmartSearch');
            setSearchResult(null);
        }
    }, [location.state]);

    // Nếu có smart search, ưu tiên hiển thị kết quả này
    useEffect(() => {
        if (searchResult) {
            setProducts(searchResult.products || []);
            setTotalPages(Math.ceil((searchResult.products?.length || 0) / itemsPerPage));
            setError(null);
            setCurrentPage(1);
        }
    }, [searchResult]);

    // Nếu không có smart search, fetch sản phẩm mặc định
    useEffect(() => {
        if (searchResult) return; // Đã xử lý ở useEffect trên

        const fetchProducts = async () => {
            try{
                let allProducts = [];
                const queryparams = new URLSearchParams(location.search);
                const searchItem = queryparams.get('search')

                if (searchItem) {
                    const response = await ApiService.searchProducts(searchItem);
                    allProducts = response.productList || [];
                } else {
                    const response = await ApiService.getAllProducts();
                    allProducts = response || [];
                }

                setTotalPages(Math.ceil(allProducts.length/itemsPerPage));
                setProducts(allProducts.slice((currentPage -1) * itemsPerPage, currentPage * itemsPerPage));
                setError(null);
            } catch(error){
                setError(error.response?.data?.message || error.message || 'unable to fetch products')
            }
        }

        fetchProducts();
    }, [location.search, currentPage, searchResult]);

    // Lấy sản phẩm trang hiện tại nếu là smart search
    const paginatedProducts = searchResult
        ? (searchResult.products || []).slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage)
        : products;

    return(
        <div className="home">
            {error ? (
                <p className="error-message">{error}</p>
            ):(
                <div>
                    {/* Nếu có smart search, hiển thị giải thích và kết quả */}
                    {searchResult && (
                        <div className="search-results">
                            <div className="search-explanation">
                                {searchResult.explanation}
                            </div>
                            {paginatedProducts.length === 0 && (
                                <div className="no-results">
                                    <p>{searchResult.explanation}</p>
                                    <p>Gợi ý: Hãy nhập từ khóa cụ thể hơn, ví dụ: "điện thoại Samsung dưới 10 triệu còn hàng"</p>
                                </div>
                            )}
                        </div>
                    )}
                    {/* Nếu không có sản phẩm, báo lên giao diện */}
                    {!searchResult && paginatedProducts.length === 0 && (
                        <div className="no-results">
                            <p>Không có sản phẩm nào để hiển thị.</p>
                        </div>
                    )}
                    <ProductList products={paginatedProducts}/>
                    <Pagination  
                        currentPage={currentPage}
                        totalPages={totalPages}
                        onPageChange={(page)=> setCurrentPage(page)}
                    />
                </div>
            )}
        </div>
    )
}

export default Home;