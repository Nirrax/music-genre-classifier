import Register from "./components/Register";
import Login from "./components/Login";
import NotFound from "./components/NotFound";
import Layout from "./components/Layout";
import Home from "./components/Home";
import Upload from "./components/Upload";
import Update from "./components/Update";
import RequireAuth from "./components/RequireAuth";
import PersistLogin from "./components/PersistLogin";
import { Routes, Route } from "react-router-dom";

function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        {/* public routes */}
        <Route path="register" element={<Register />} />
        <Route path="login" element={<Login />} />

        {/* protected routes */}
        <Route element={<PersistLogin />}>
          <Route element={<RequireAuth />}>
            <Route path="home" element={<Home />} />
            <Route path="upload" element={<Upload />} />
            <Route path="update" element={<Update />} />
          </Route>
        </Route>

        {/* catch-all route */}
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  );
}

export default App;
