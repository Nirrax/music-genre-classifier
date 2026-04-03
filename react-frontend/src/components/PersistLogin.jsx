import { Outlet } from "react-router-dom";
import { useState, useEffect } from "react";
import useRefreshToken from "../hooks/useRefreshToken";
import useAuth from "../hooks/useAuth";

const PersistLogin = () => {
  const [isLoading, setIsLoading] = useState(true);
  const refresh = useRefreshToken();
  const { auth } = useAuth();

  useEffect(() => {
    const verifyRefreshToken = async () => {
      try {
        await refresh();
      } catch (err) {
        console.error(err);
      } finally {
        setIsLoading(false);
      }

      !auth?.token && auth?.refreshToken
        ? verifyRefreshToken()
        : setIsLoading(false);
    };

    verifyRefreshToken();
  }, []);

  useEffect(() => {
    console.log(`isLoading: ${isLoading}`);
    console.log(`auth: ${JSON.stringify(auth)}`);
  }, [isLoading]);

  return <>{isLoading ? <p>Loading...</p> : <Outlet />}</>;
};

export default PersistLogin;
