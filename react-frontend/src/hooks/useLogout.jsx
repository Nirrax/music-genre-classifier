import axios from "../api/axios";
import useAuth from "./useAuth";

const LOGOUT_URL = "/auth/logout";

const useLogout = () => {
  const { auth, setAuth } = useAuth();

  const logout = async () => {
    try {
      const response = await axios.post(
        LOGOUT_URL,
        { refreshToken: auth?.refreshToken },
        {
          headers: { "Content-Type": "application/json" },
          withCredentials: true,
        },
      );
      setAuth({});
    } catch (error) {
      console.error(error);
    }
  };

  return logout;
};

export default useLogout;
