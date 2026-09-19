import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { Mail, Lock, Sparkles } from "lucide-react";

import api from "../api/axios";

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const navigate = useNavigate();

  const login = async () => {
    if (!email.trim() || !password.trim()) {
      toast.error("Please enter both email and password");
      return;
    }

    setSubmitting(true);

    try {
      const res = await api.post("/auth/login", {
        email,
        password,
      });

      localStorage.setItem("token", res.data.token);

      toast.success("Welcome back!");

      navigate("/dashboard");
    } catch (error: any) {
      console.log("Login error:", error.response || error);

      toast.error(
        error.response?.data?.message || "Login failed"
      );
    } finally {
      setSubmitting(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") {
      login();
    }
  };

  return (
    <div
      className="
        min-h-screen
        bg-gradient-to-br
        from-[#020617]
        via-[#07122b]
        to-[#020617]
        flex
        items-center
        justify-center
        p-5
        text-white
      "
    >
      <div
        className="
          w-full
          max-w-md
          bg-white/5
          border
          border-white/10
          backdrop-blur-2xl
          rounded-[32px]
          p-8
          shadow-2xl
        "
      >
        {/* BRAND */}
        <div className="flex items-center gap-4 mb-8">
          <div
            className="
              w-14
              h-14
              rounded-2xl
              bg-gradient-to-r
              from-blue-500
              to-cyan-400
              flex
              items-center
              justify-center
              shadow-[0_0_30px_rgba(59,130,246,0.35)]
            "
          >
            <Sparkles size={26} />
          </div>

          <div>
            <h1 className="text-2xl font-black leading-tight">
              Management System
            </h1>
            <p className="text-gray-400 text-sm">
              Sign in to continue
            </p>
          </div>
        </div>

        <div className="flex flex-col gap-4" onKeyDown={handleKeyDown}>
          <div className="flex flex-col gap-2">
            <label
              className="
                text-gray-300
                text-sm
                font-semibold
                flex
                items-center
                gap-2
              "
            >
              <Mail size={15} className="text-blue-400" />
              Email
            </label>
            <input
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="
                bg-white/5
                border
                border-white/10
                rounded-2xl
                px-5
                py-3
                text-white
                outline-none
                placeholder:text-gray-500
                focus:border-blue-400/50
                transition
              "
            />
          </div>

          <div className="flex flex-col gap-2">
            <div className="flex items-center justify-between">
              <label
                className="
                  text-gray-300
                  text-sm
                  font-semibold
                  flex
                  items-center
                  gap-2
                "
              >
                <Lock size={15} className="text-blue-400" />
                Password
              </label>

              <Link
                to="/forgot-password"
                className="text-blue-400 text-xs hover:underline"
              >
                Forgot password?
              </Link>
            </div>
            <input
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="
                bg-white/5
                border
                border-white/10
                rounded-2xl
                px-5
                py-3
                text-white
                outline-none
                placeholder:text-gray-500
                focus:border-blue-400/50
                transition
              "
            />
          </div>

          <button
            onClick={login}
            disabled={submitting}
            className="
              mt-2
              bg-gradient-to-r
              from-blue-500
              to-cyan-500
              hover:scale-[1.02]
              transition-all
              duration-300
              px-6
              py-3
              rounded-2xl
              font-semibold
              shadow-lg
              shadow-blue-500/20
              disabled:opacity-50
              disabled:cursor-not-allowed
              disabled:hover:scale-100
            "
          >
            {submitting ? "Logging in..." : "Login"}
          </button>
        </div>
      </div>
    </div>
  );
}

export default Login;