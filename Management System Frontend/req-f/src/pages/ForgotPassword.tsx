import { useState } from "react";
import { Link } from "react-router-dom";
import toast from "react-hot-toast";
import { Mail, ArrowLeft } from "lucide-react";
import api from "../api/axios";

function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [sent, setSent] = useState(false);

  const handleSubmit = async () => {
    if (!email.trim()) {
      toast.error("Please enter your email");
      return;
    }

    setSubmitting(true);

    try {
      await api.post("/auth/forgot-password", { email });
      setSent(true);
    } catch (error) {
      console.log(error);
      toast.error("Something went wrong. Please try again.");
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
        <Link
          to="/login"
          className="
            flex
            items-center
            gap-2
            text-gray-400
            hover:text-white
            transition
            mb-6
            text-sm
          "
        >
          <ArrowLeft size={16} />
          Back to login
        </Link>

        <h1 className="text-3xl font-black mb-2">Forgot Password?</h1>
        <p className="text-gray-400 mb-8">
          Enter your email and we'll send you a link to reset it.
        </p>

        {sent ? (
          <div className="bg-green-500/10 border border-green-500/20 rounded-2xl p-6 text-center">
            <p className="text-green-400 font-semibold">
              Check your inbox
            </p>
            <p className="text-gray-400 text-sm mt-2">
              If that email exists in our system, a reset link is on
              its way.
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
              {submitting ? "Sending..." : "Send Reset Link"}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default ForgotPassword;