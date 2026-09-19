
import { useEffect, useState } from "react";
import {
  CheckCircle2,
  XCircle,
  Clock3,
  Loader2,
  AlertCircle,
} from "lucide-react";
import toast from "react-hot-toast";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import api from "../api/axios";

function ManagerRequests() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState(null);
  const [error, setError] = useState("");

  const loadRequests = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get(
        "/requests/pending-approvals"
      );

      setRequests(response.data);

    } catch (err) {
      console.error(err);

      const message =
        err.response?.data?.message ||
        "Failed to load pending requests";

      setError(message);
      toast.error(message);

    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRequests();
  }, []);

  const approve = async (id) => {
    try {
      setBusyId(id);

      await api.put(
        `/requests/${id}/approve`
      );

      toast.success(
        "Request approved successfully"
      );

      await loadRequests();

    } catch (err) {
      console.error(err);

      toast.error(
        err.response?.data?.message ||
          "Failed to approve request"
      );

    } finally {
      setBusyId(null);
    }
  };

  const reject = async (id) => {
    try {
      setBusyId(id);

      await api.put(
        `/requests/${id}/reject`
      );

      toast.success(
        "Request rejected successfully"
      );

      await loadRequests();

    } catch (err) {
      console.error(err);

      toast.error(
        err.response?.data?.message ||
          "Failed to reject request"
      );

    } finally {
      setBusyId(null);
    }
  };

  return (
    <div
      className="flex min-h-screen text-white"
      style={{
        backgroundColor: "rgb(2, 6, 23)",
      }}
    >
      <Sidebar />

      <div className="flex-1 min-w-0">
        <Navbar />

        <main className="p-6 lg:p-8">
          <div className="max-w-6xl mx-auto">

            <div className="mb-8">
              <h1 className="text-3xl font-bold">
                Manager Approvals
              </h1>

              <p className="text-slate-400 mt-1">
                Review requests assigned to you
              </p>
            </div>

            {loading ? (
              <div className="flex justify-center py-20">
                <Loader2
                  size={30}
                  className="animate-spin text-blue-400"
                />
              </div>
            ) : error ? (
              <div className="
                flex items-center justify-center gap-2
                text-red-400
                py-16
              ">
                <AlertCircle size={18} />
                {error}
              </div>
            ) : requests.length === 0 ? (
              <div className="
                bg-white/5
                border border-white/10
                rounded-[28px]
                p-10
                text-center
              ">
                <Clock3
                  size={38}
                  className="mx-auto text-slate-500"
                />

                <h2 className="text-xl font-semibold mt-4">
                  No pending requests
                </h2>

                <p className="text-slate-400 mt-2">
                  There are no requests waiting for your approval.
                </p>
              </div>
            ) : (
              <div className="space-y-4">
                {requests.map((request) => (
                  <div
                    key={request.id}
                    className="
                      bg-white/5
                      border border-white/10
                      rounded-[28px]
                      p-6
                    "
                  >
                    <div className="
                      flex
                      flex-col
                      lg:flex-row
                      lg:items-center
                      lg:justify-between
                      gap-5
                    ">

                      <div>
                        <div className="flex items-center gap-3">
                          <span className="
                            px-3 py-1.5
                            rounded-xl
                            bg-yellow-500/20
                            text-yellow-400
                            text-sm
                            font-semibold
                          ">
                            PENDING
                          </span>

                          <span className="
                            text-slate-500
                            text-sm
                          ">
                            #{request.id}
                          </span>
                        </div>

                        <h2 className="
                          text-xl
                          font-bold
                          mt-3
                        ">
                          {request.title}
                        </h2>

                        <p className="
                          text-slate-400
                          mt-2
                          max-w-3xl
                        ">
                          {request.description}
                        </p>

                        <p className="
                          text-sm
                          text-slate-500
                          mt-3
                        ">
                          Requested by:{" "}
                          {request.user?.name || "Unknown"}
                        </p>
                      </div>

                      <div className="
                        flex
                        gap-3
                        shrink-0
                      ">
                        <button
                          type="button"
                          disabled={busyId === request.id}
                          onClick={() =>
                            approve(request.id)
                          }
                          className="
                            inline-flex
                            items-center
                            gap-2
                            px-5 py-3
                            rounded-2xl
                            bg-green-600
                            hover:bg-green-700
                            font-semibold
                            transition
                            disabled:opacity-50
                          "
                        >
                          {busyId === request.id ? (
                            <Loader2
                              size={18}
                              className="animate-spin"
                            />
                          ) : (
                            <CheckCircle2 size={18} />
                          )}

                          Approve
                        </button>

                        <button
                          type="button"
                          disabled={busyId === request.id}
                          onClick={() =>
                            reject(request.id)
                          }
                          className="
                            inline-flex
                            items-center
                            gap-2
                            px-5 py-3
                            rounded-2xl
                            bg-red-600
                            hover:bg-red-700
                            font-semibold
                            transition
                            disabled:opacity-50
                          "
                        >
                          <XCircle size={18} />

                          Reject
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}

          </div>
        </main>
      </div>
    </div>
  );
}

export default ManagerRequests;

