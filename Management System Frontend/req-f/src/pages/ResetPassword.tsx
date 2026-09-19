import { useState } from "react";
import { useNavigate, useSearchParams, Link } from "react-router-dom";
import toast from "react-hot-toast";
import { Lock } from "lucide-react";
import api from "../api/axios";

function ResetPassword() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get("token") || "";

  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async () => {
    if (!token) {
      toast.error("Missing or invalid reset link");
      return;
    }

    if (newPassword.length < 6) {
      toast.error("Password must be at least 6 characters");
      return;
    }

    if (newPassword !== confirmPassword) {
      toast.error("Passwords don't match");
      return;
    }

    setSubmitting(true);

    try {
      await api.post("/auth/reset-password", {
        token,
        newPassword,
      });

      toast.success("Password reset! You can log in now.");
      navigate("/login");
    } catch (error: any) {
      const message =
        error.response?.data?.message || "Failed to reset password";
      toast.error(message);
    } finally {
      setSubmitting(false);
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
        <h1 className="text-3xl font-black mb-2">Reset Password</h1>
        <p className="text-gray-400 mb-8">
          Choose a new password for your account.
        </p>

        {!token ? (
          <div className="bg-red-500/10 border border-red-500/20 rounded-2xl p-6 text-center">
            <p className="text-red-400 font-semibold">Invalid link</p>
            <p className="text-gray-400 text-sm mt-2">
              This reset link is missing or malformed.{" "}
              <Link
                to="/forgot-password"
                className="text-blue-400 underline"
              >
                Request a new one
              </Link>
            </p>
          </div>
        ) : (
          <div className="flex flex-col gap-4">
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
                <Lock size={15} className="text-blue-400" />
                New Password
              </label>
              <input
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                className="
                  bg-white/5
                  border
                  border-white/10
                  rounded-2xl
                  px-5
                  py-3
                  text-white
                  outline-none
                  focus:border-blue-400/50
                  transition
                "
              />
            </div>

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
                <Lock size={15} className="text-blue-400" />
                Confirm Password
              </label>
              <input
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="
                  bg-white/5
                  border
                  border-white/10
                  rounded-2xl
                  px-5
                  py-3
                  text-white
                  outline-none
                  focus:border-blue-400/50
                  transition
                "
              />
            </div>

            <button
              onClick={handleSubmit}
              disabled={submitting}
              className="
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
              {submitting ? "Resetting..." : "Reset Password"}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default ResetPassword;