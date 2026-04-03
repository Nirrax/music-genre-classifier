import { Outlet, useLocation, Navigate } from "react-router-dom";

const Layout = () => {
  const location = useLocation();

  if (location.pathname === "/") {
    return <Navigate to="/home" replace />;
  }

  return (
    <main className="App">
      <Outlet />
    </main>
  );
};

export default Layout;
