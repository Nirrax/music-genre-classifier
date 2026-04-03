import { useState, useEffect } from "react";
import useAxiosPrivate from "../hooks/useAxiosPrivate";
import { useNavigate, useLocation } from "react-router-dom";
import useLogout from "../hooks/useLogout";

const Profile = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const axiosPrivate = useAxiosPrivate();
  const navigate = useNavigate();
  const location = useLocation();
  const logout = useLogout();

  useEffect(() => {
    let isMounted = true;
    const controller = new AbortController();

    const getUser = async () => {
      try {
        const response = await axiosPrivate.get("/users/me", {
          signal: controller.signal,
        });
        console.log(response.data);
        isMounted && setUser(response.data);
        isMounted && setLoading(false);
      } catch (err) {
        console.error(err);
        navigate("/login", { state: { from: location }, replace: true });
      }
    };

    getUser();

    return () => {
      isMounted = false;
      controller.abort();
    };
  }, []);

  const handleSignOut = async () => {
    await logout();
    navigate("/login", { state: { from: location }, replace: true });
  };

  const handlePasswordUpdate = () => {
    navigate("/update");
  };

  if (loading) {
    return (
      <section>
        <p>Loading user data...</p>
      </section>
    );
  }

  return (
    <section>
      <h1>User Profile</h1>

      <div style={{ marginTop: "2rem" }}>
        <div style={{ marginBottom: "1.5rem" }}>
          <label style={{ fontSize: "0.9rem", opacity: 0.8 }}>User ID</label>
          <p
            style={{
              marginTop: "0.5rem",
              padding: "0.75rem",
              background: "rgba(255, 255, 255, 0.1)",
              borderRadius: "0.5rem",
              wordBreak: "break-all",
              fontSize: "0.8rem",
            }}
          >
            {user?.id}
          </p>
        </div>

        <div style={{ marginBottom: "1.5rem" }}>
          <label style={{ fontSize: "0.9rem", opacity: 0.8 }}>Username</label>
          <p
            style={{
              marginTop: "0.5rem",
              padding: "0.75rem",
              background: "rgba(255, 255, 255, 0.1)",
              borderRadius: "0.5rem",
              fontSize: "1rem",
              fontWeight: "bold",
            }}
          >
            {user?.username}
          </p>
        </div>

        <div style={{ marginBottom: "1.5rem" }}>
          <label style={{ fontSize: "0.9rem", opacity: 0.8 }}>
            Member Since
          </label>
          <p
            style={{
              marginTop: "0.5rem",
              padding: "0.75rem",
              background: "rgba(255, 255, 255, 0.1)",
              borderRadius: "0.5rem",
              fontSize: "0.9rem",
            }}
          >
            {user?.createdAt &&
              new Date(user.createdAt).toLocaleDateString("en-US", {
                year: "numeric",
                month: "long",
                day: "numeric",
              })}
          </p>
        </div>
        <div className="buttons-container">
          <button onClick={handlePasswordUpdate}>Update Password</button>
          <button onClick={handleSignOut}>Sign Out</button>
        </div>
      </div>
    </section>
  );
};

export default Profile;
