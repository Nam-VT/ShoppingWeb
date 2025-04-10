import React from "react";
import '../../style/footer.css';
import { NavLink } from "react-router-dom";

const Footer = () => {

    return (
        <footer className="footer">
            <div className="footer-links">
                <ul>
                    <NavLink to={"/"}>About Us</NavLink>
                    <NavLink to={"/"}>Contact Us</NavLink>
                    <NavLink to={"/"}>Terms & Cnnditions</NavLink>
                    <NavLink to={"/"}>Privacy Policy</NavLink>
                    <NavLink to={"/"}>FAQs</NavLink>
                </ul>
            </div>
        </footer>
    )
}
export default Footer;