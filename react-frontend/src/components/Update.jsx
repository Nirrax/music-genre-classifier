import { useRef, useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import {
  faCheck,
  faTimes,
  faInfoCircle,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import useAxiosPrivate from "../hooks/useAxiosPrivate";

const PWD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%]).{8,24}$/;

const UPDATE_URL = "/users/me";

const ChangePassword = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const axiosPrivate = useAxiosPrivate();

  const currentPwdRef = useRef();
  const errRef = useRef();

  const [currentPwd, setCurrentPwd] = useState("");
  const [currentPwdFocus, setCurrentPwdFocus] = useState(false);

  const [newPwd, setNewPwd] = useState("");
  const [validPwd, setValidPwd] = useState(false);
  const [pwdFocus, setPwdFocus] = useState(false);

  const [matchPwd, setMatchPwd] = useState("");
  const [validMatch, setValidMatch] = useState(false);
  const [matchFocus, setMatchFocus] = useState(false);

  const [errMsg, setErrMsg] = useState("");
  const [successMsg, setSuccessMsg] = useState("");

  useEffect(() => {
    currentPwdRef.current.focus();
  }, []);

  useEffect(() => {
    setValidPwd(PWD_REGEX.test(newPwd));
    setValidMatch(newPwd === matchPwd);
  }, [newPwd, matchPwd]);

  useEffect(() => {
    setErrMsg("");
    setSuccessMsg("");
  }, [currentPwd, newPwd, matchPwd]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Validation
    const v1 = PWD_REGEX.test(newPwd);
    if (!v1) {
      setErrMsg("Invalid Password Entry");
      return;
    }
    if (!currentPwd) {
      setErrMsg("Current password is required");
      return;
    }

    try {
      const response = await axiosPrivate.patch(
        UPDATE_URL,
        { currentPassword: currentPwd, newPassword: newPwd },
        {
          headers: { "Content-Type": "application/json" },
        },
      );

      console.log(response?.data);
      setSuccessMsg("Password updated successfully!");

      // Navigate back to profile after 2 seconds
      setTimeout(() => {
        setCurrentPwd("");
        setNewPwd("");
        setMatchPwd("");
        navigate("/home", { replace: true });
      }, 1000);
    } catch (err) {
      console.error(err);
      if (!err?.response) {
        setErrMsg("No Server Response");
      } else if (err.response?.status === 401) {
        setErrMsg("Current password is incorrect");
      } else {
        setErrMsg(err?.response?.data?.message || "Password update failed");
      }
      errRef.current.focus();
    }
  };

  return (
    <section>
      <p
        ref={errRef}
        className={errMsg ? "errmsg" : "offscreen"}
        aria-live="assertive"
      >
        {errMsg}
      </p>
      {successMsg && (
        <p
          style={{
            backgroundColor: "lightgreen",
            color: "darkgreen",
            fontWeight: "bold",
            padding: "0.5rem",
            marginBottom: "0.5rem",
            borderRadius: "0.5rem",
          }}
        >
          {successMsg}
        </p>
      )}
      <h1>Change Password</h1>
      <form onSubmit={handleSubmit}>
        <label htmlFor="current_password">Current Password:</label>
        <input
          type="password"
          id="current_password"
          ref={currentPwdRef}
          onChange={(e) => setCurrentPwd(e.target.value)}
          value={currentPwd}
          required
          onFocus={() => setCurrentPwdFocus(true)}
          onBlur={() => setCurrentPwdFocus(false)}
        />

        <label htmlFor="new_password">
          New Password:
          <FontAwesomeIcon
            icon={faCheck}
            className={validPwd ? "valid" : "hide"}
          />
          <FontAwesomeIcon
            icon={faTimes}
            className={validPwd || !newPwd ? "hide" : "invalid"}
          />
        </label>
        <input
          type="password"
          id="new_password"
          onChange={(e) => setNewPwd(e.target.value)}
          value={newPwd}
          required
          aria-invalid={validPwd ? "false" : "true"}
          aria-describedby="pwdnote"
          onFocus={() => setPwdFocus(true)}
          onBlur={() => setPwdFocus(false)}
        />
        <p
          id="pwdnote"
          className={pwdFocus && !validPwd ? "instructions" : "offscreen"}
        >
          <FontAwesomeIcon icon={faInfoCircle} />
          8 to 24 characters.
          <br />
          Must include uppercase and lowercase letters, a number and a special
          character.
          <br />
          Allowed special characters:{" "}
          <span aria-label="exclamation mark">!</span>{" "}
          <span aria-label="at symbol">@</span>{" "}
          <span aria-label="hashtag">#</span>{" "}
          <span aria-label="dollar sign">$</span>{" "}
          <span aria-label="percent">%</span>
        </p>

        <label htmlFor="confirm_pwd">
          Confirm New Password:
          <FontAwesomeIcon
            icon={faCheck}
            className={validMatch && matchPwd ? "valid" : "hide"}
          />
          <FontAwesomeIcon
            icon={faTimes}
            className={validMatch || !matchPwd ? "hide" : "invalid"}
          />
        </label>
        <input
          type="password"
          id="confirm_pwd"
          onChange={(e) => setMatchPwd(e.target.value)}
          value={matchPwd}
          required
          aria-invalid={validMatch ? "false" : "true"}
          aria-describedby="confirmnote"
          onFocus={() => setMatchFocus(true)}
          onBlur={() => setMatchFocus(false)}
        />
        <p
          id="confirmnote"
          className={matchFocus && !validMatch ? "instructions" : "offscreen"}
        >
          <FontAwesomeIcon icon={faInfoCircle} />
          Must match the new password input field.
        </p>

        <button disabled={!currentPwd || !validPwd || !validMatch}>
          Update Password
        </button>
      </form>
      <p>
        <span className="line">
          <button type="button" onClick={() => navigate("/home")}>
            Back to Profile
          </button>
        </span>
      </p>
    </section>
  );
};

export default ChangePassword;
