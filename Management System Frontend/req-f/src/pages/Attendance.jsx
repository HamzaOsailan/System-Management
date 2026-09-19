
import { useEffect, useState } from "react";
import {
  Fingerprint,
  LogIn,
  LogOut,
  ShieldCheck,
  CalendarDays,
  Loader2,
  AlertCircle,
  CheckCircle2,
} from "lucide-react";
import toast from "react-hot-toast";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import api from "../api/axios";

const BG = "rgb(2, 6, 23)";

function Attendance() {
  const [attendance, setAttendance] = useState(null);
  const [registered, setRegistered] = useState(false);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  const normalizeWebAuthnOptions = (data) => {
    let options = data;

    // In case Axios receives JSON as a string
    if (typeof options === "string") {
      options = JSON.parse(options);
    }

    // In case the API wraps the WebAuthn object
    if (options?.publicKey) {
      options = options.publicKey;
    }

    return options;
  };

  const load = async () => {
    try {
      setLoading(true);
      setError("");

      const [today, biometric] = await Promise.all([
        api.get("/attendance/today"),
        api.get("/attendance/webauthn/status"),
      ]);

      setAttendance(today.data);

      // Backend returns true/false directly
      setRegistered(Boolean(biometric.data));
    } catch (err) {
      console.error(err);

      setError(
        err.response?.data?.message ||
          "Failed to load attendance"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const registerBiometric = async () => {
    try {
      setBusy(true);
      setError("");

      if (!window.PublicKeyCredential) {
        throw new Error(
          "Biometric authentication is not supported by this browser."
        );
      }

      const response = await api.post(
        "/attendance/webauthn/register/start"
      );

      const options = normalizeWebAuthnOptions(response.data);

      console.log(
        "WebAuthn registration options:",
        options
      );

      if (!options?.challenge) {
        console.error(
          "Invalid WebAuthn registration options:",
          options
        );

        throw new Error(
          "WebAuthn challenge is missing from the server response."
        );
      }

      const publicKey =
        PublicKeyCredential.parseCreationOptionsFromJSON(
          options
        );

      const credential =
        await navigator.credentials.create({
          publicKey,
        });

      if (!credential) {
        throw new Error(
          "Biometric registration was cancelled."
        );
      }

      await api.post(
        "/attendance/webauthn/register/finish",
        credential.toJSON()
      );

      setRegistered(true);

      toast.success(
        "Biometric authentication registered successfully"
      );

      await load();

    } catch (err) {
      console.error(
        "WebAuthn registration error:",
        err
      );

      const message =
        err.response?.data?.message ||
        err.message ||
        "Failed to register biometric authentication";

      setError(message);
      toast.error(message);

    } finally {
      setBusy(false);
    }
  };

  const checkInOrOut = async () => {
    try {
      setBusy(true);
      setError("");

      const response = await api.post(
        "/attendance/webauthn/authenticate/start"
      );

      const options = normalizeWebAuthnOptions(
        response.data
      );

      console.log(
        "WebAuthn authentication options:",
        options
      );

      if (!options?.challenge) {
        console.error(
          "Invalid WebAuthn authentication options:",
          options
        );

        throw new Error(
          "WebAuthn challenge is missing from the server response."
        );
      }

      const publicKey =
        PublicKeyCredential.parseRequestOptionsFromJSON(
          options
        );

      const credential =
        await navigator.credentials.get({
          publicKey,
        });

      if (!credential) {
        throw new Error(
          "Biometric verification was cancelled."
        );
      }

      const finishResponse = await api.post(
        "/attendance/webauthn/authenticate/finish",
        credential.toJSON()
      );

      const data = finishResponse.data;

      setAttendance(data);

      toast.success(
        data.status === "CHECKED_IN"
          ? "Check-in recorded successfully"
          : "Check-out recorded successfully"
      );

    } catch (err) {
      console.error(
        "WebAuthn authentication error:",
        err
      );

      const message =
        err.response?.data?.message ||
        err.message ||
        "Biometric verification failed";

      setError(message);
      toast.error(message);

    } finally {
      setBusy(false);
    }
  };

  const status =
    attendance?.status || "NOT_CHECKED_IN";

  if (loading) {
    return (
      <div
        className="flex min-h-screen text-white"
        style={{ backgroundColor: BG }}
      >
        <Sidebar />

        <div className="flex-1">
          <Navbar />

          <div className="flex justify-center items-center h-[70vh]">
            <Loader2
              size={28}
              className="animate-spin text-blue-400"
            />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div
      className="flex min-h-screen text-white"
      style={{ backgroundColor: BG }}
    >
      <Sidebar />

      <div className="flex-1 min-w-0">
        <Navbar />

        <main className="p-4 sm:p-6 lg:p-8">
          <div className="max-w-5xl mx-auto">

            {/* HEADER */}
            <div className="mb-8">
              <h1 className="text-3xl font-bold">
                Attendance
              </h1>

              <p className="text-slate-400 mt-1">
                Secure daily attendance using your device
                verification
              </p>
            </div>

            {/* MAIN CARD */}
            <div
              className="
                bg-white/5
                border border-white/10
                rounded-[28px]
                overflow-hidden
              "
            >
              <div className="p-6 sm:p-8">

                {/* DATE */}
                <div className="flex items-center gap-2 text-slate-400 mb-8">
                  <CalendarDays size={18} />

                  <span>
                    {new Date().toLocaleDateString(
                      undefined,
                      {
                        weekday: "long",
                        year: "numeric",
                        month: "long",
                        day: "numeric",
                      }
                    )}
                  </span>
                </div>

                {/* STATUS */}
                <div className="text-center">
                  <div
                    className="
                      w-24 h-24
                      mx-auto
                      rounded-full
                      bg-blue-500/10
                      border border-blue-500/20
                      flex items-center justify-center
                    "
                  >
                    {status === "CHECKED_OUT" ? (
                      <CheckCircle2
                        size={42}
                        className="text-green-400"
                      />
                    ) : (
                      <Fingerprint
                        size={42}
                        className="text-blue-400"
                      />
                    )}
                  </div>

                  <h2 className="text-2xl font-bold mt-5">
                    {status === "NOT_CHECKED_IN"
                      ? "Ready to check in"
                      : status === "CHECKED_IN"
                      ? "You're checked in"
                      : "Attendance completed"}
                  </h2>

                  <p className="text-slate-400 mt-2">
                    {status === "NOT_CHECKED_IN"
                      ? "Verify your identity using your device"
                      : status === "CHECKED_IN"
                      ? "You can check out when you finish your work"
                      : "Your attendance for today is complete"}
                  </p>
                </div>

                {/* REGISTER */}
                {!registered && (
                  <div
                    className="
                      mt-8
                      p-5
                      rounded-2xl
                      bg-purple-500/5
                      border border-purple-500/20
                    "
                  >
                    <div className="flex gap-4">
                      <Fingerprint
                        size={24}
                        className="text-purple-400 shrink-0"
                      />

                      <div>
                        <h3 className="font-semibold">
                          Set up biometric verification
                        </h3>

                        <p className="text-sm text-slate-400 mt-1">
                          Register your device once to use
                          fingerprint, Face ID, or your device
                          security verification.
                        </p>

                        <button
                          type="button"
                          onClick={registerBiometric}
                          disabled={busy}
                          className="
                            mt-4
                            inline-flex items-center gap-2
                            px-5 py-2.5
                            rounded-2xl
                            bg-purple-600
                            text-white
                            font-semibold
                            hover:bg-purple-700
                            transition
                            disabled:opacity-50
                          "
                        >
                          {busy ? (
                            <Loader2
                              size={18}
                              className="animate-spin"
                            />
                          ) : (
                            <Fingerprint size={18} />
                          )}

                          Set Up
                        </button>
                      </div>
                    </div>
                  </div>
                )}

                {/* ACTION */}
                {registered && (
                  <div className="flex justify-center mt-9">
                    <button
                      type="button"
                      disabled={
                        busy ||
                        status === "CHECKED_OUT"
                      }
                      onClick={checkInOrOut}
                      className={`
                        min-w-[220px]
                        inline-flex
                        items-center
                        justify-center
                        gap-3
                        px-7 py-3.5
                        rounded-2xl
                        font-semibold
                        transition
                        disabled:opacity-50
                        disabled:cursor-not-allowed
                        ${
                          status === "CHECKED_IN"
                            ? "bg-red-600 hover:bg-red-700"
                            : "bg-blue-600 hover:bg-blue-700"
                        }
                      `}
                    >
                      {busy ? (
                        <Loader2
                          size={21}
                          className="animate-spin"
                        />
                      ) : status === "CHECKED_IN" ? (
                        <LogOut size={21} />
                      ) : (
                        <LogIn size={21} />
                      )}

                      {status === "CHECKED_IN"
                        ? "Check Out"
                        : status === "CHECKED_OUT"
                        ? "Completed"
                        : "Check In"}
                    </button>
                  </div>
                )}

                {/* ERROR */}
                {error && (
                  <div
                    className="
                      flex items-center justify-center gap-2
                      mt-6
                      text-sm
                      text-red-400
                    "
                  >
                    <AlertCircle size={17} />
                    {error}
                  </div>
                )}
              </div>

              {/* TIMES */}
              <div
                className="
                  grid
                  grid-cols-1
                  sm:grid-cols-2
                  border-t
                  border-white/10
                "
              >
                <div
                  className="
                    p-6
                    border-b
                    sm:border-b-0
                    sm:border-r
                    border-white/10
                  "
                >
                  <p className="text-sm text-slate-500">
                    Check In
                  </p>

                  <p className="text-xl font-bold mt-2">
                    {attendance?.checkIn
                      ? new Date(
                          attendance.checkIn
                        ).toLocaleTimeString()
                      : "--:--"}
                  </p>
                </div>

                <div className="p-6">
                  <p className="text-sm text-slate-500">
                    Check Out
                  </p>

                  <p className="text-xl font-bold mt-2">
                    {attendance?.checkOut
                      ? new Date(
                          attendance.checkOut
                        ).toLocaleTimeString()
                      : "--:--"}
                  </p>
                </div>
              </div>
            </div>

            {/* SECURITY NOTE */}
            <div
              className="
                flex items-center gap-3
                mt-5
                px-5 py-4
                rounded-2xl
                bg-green-500/5
                border border-green-500/10
                text-sm text-slate-400
              "
            >
              <ShieldCheck
                size={19}
                className="text-green-400 shrink-0"
              />

              <span>
                Your biometric data stays on your device.
                Sahab stores the WebAuthn credential needed
                to verify your attendance.
              </span>
            </div>

          </div>
        </main>
      </div>
    </div>
  );
}

export default Attendance;

