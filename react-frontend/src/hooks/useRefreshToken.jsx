import axios from "../api/axios";
import useAuth from "./useAuth";

const REFRESH_URL = "/auth/refresh";

const useRefreshToken = () => {
  const { auth, setAuth } = useAuth();

  const refresh = async () => {
    const response = await axios.post(
      REFRESH_URL,
      { refreshToken: auth?.refreshToken },
      {
        headers: { "Content-Type": "application/json" },
        withCredentials: true,
      },
    );
    setAuth((prev) => {
      console.log(JSON.stringify(prev));
      console.log(response.data.token);
      return {
        ...prev,
        token: response.data.token,
        refreshToken: response.data.refreshToken,
      };
    });
    return response.data.token;
  };

  return refresh;
};

export default useRefreshToken;
