
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import {
  Pencil,
  Trash2,
  Sparkles,
  Eye,
  Plus,
  FileText,
  Clock3,
  CheckCircle2,
  XCircle,
  Car,
  Laptop,
  CalendarDays,
  Users,
  FolderOpen,
  RefreshCw,
} from "lucide-react";

import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import api from "../api/axios";

const categoryStyles = {
  IT: "bg-cyan-50 text-cyan-700",
  FLEET: "bg-indigo-50 text-indigo-700",
  LEAVE: "bg-purple-50 text-purple-700",
  HR: "bg-orange-50 text-orange-700",
  GENERAL: "bg-gray-100 text-gray-700",
};

const categoryIcons = {
  IT: Laptop,
  FLEET: Car,
  LEAVE: CalendarDays,
  HR: Users,
  GENERAL: FolderOpen,
};

const statusStyles = {
  APPROVED: "bg-green-50 text-green-700",
  REJECTED: "bg-red-50 text-red-700",
  PENDING: "bg-yellow-50 text-yellow-700",
};

const statusIcons = {
  APPROVED: CheckCircle2,
  REJECTED: XCircle,
  PENDING: Clock3,
};

function ActionButton({
  icon: Icon,
  children,
  className,
  onClick,
  disabled = false,
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      className={`
        inline-flex items-center justify-center gap-2
        px-3.5 py-2
        rounded-lg
        text-sm font-semibold
        transition
        disabled:opacity-50
        disabled:cursor-not-allowed
        ${className}
      `}
    >
      {Icon && <Icon size={15} />}
      {children}
    </button>
  );
}

function AdminMyRequests() {
  const navigate = useNavigate();

  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [deletingId, setDeletingId] = useState(null);

  // =====================================================
  // FETCH REQUESTS
  // =====================================================

  const fetchMyRequests = async () => {
    try {
      setLoading(true);

      const { data } = await api.get("/requests/my");

      setRequests(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error("Failed to fetch requests:", error);

      toast.error(
        error.response?.data?.message ||
          "Failed to load requests"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMyRequests();
  }, []);

  // =====================================================
  // DELETE
  // =====================================================

  const handleDelete = async (id) => {
    if (
      !window.confirm(
        "Are you sure you want to delete this request?"
      )
    ) {
      return;
    }

    try {
      setDeletingId(id);

      await api.delete(`/requests/${id}`);

      setRequests((prev) =>
        prev.filter((request) => request.id !== id)
      );

      toast.success("Request deleted successfully");
    } catch (error) {
      console.error("Delete request error:", error);

      toast.error(
        error.response?.data?.message ||
          "Failed to delete request"
      );
    } finally {
      setDeletingId(null);
    }
  };

  // =====================================================
  // LOADING
  // =====================================================

  if (loading) {
    return (
      <div
        className="flex min-h-screen"
        style={{ backgroundColor: "rgb(2, 6, 23)" }}
      >
        <Sidebar />

        <div className="flex-1 min-w-0">
          <Navbar />

          <main className="p-6 lg:p-8">
            <div className="max-w-7xl mx-auto">
              <div className="space-y-3">
                <div className="h-8 w-56 bg-slate-800 rounded-lg animate-pulse" />
                <div className="h-4 w-72 bg-slate-800 rounded animate-pulse" />
              </div>

              <div className="space-y-4 mt-8">
                {[1, 2, 3].map((item) => (
                  <div
                    key={item}
                    className="h-52 bg-white/95 rounded-2xl animate-pulse"
                  />
                ))}
              </div>
            </div>
          </main>
        </div>
      </div>
    );
  }

  // =====================================================
  // PAGE
  // =====================================================

  return (
    <div
      className="flex min-h-screen"
      style={{ backgroundColor: "rgb(2, 6, 23)" }}
    >
      <Sidebar />

      <div className="flex-1 min-w-0">
        <Navbar />

        <main className="p-4 sm:p-6 lg:p-8">
          <div className="max-w-7xl mx-auto">

            {/* ================================================= */}
            {/* HEADER */}
            {/* ================================================= */}

            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-7">
              <div>
                <h1 className="text-2xl sm:text-3xl font-bold text-white">
                  My Requests
                </h1>

                <p className="text-sm text-slate-400 mt-1">
                  View and manage your submitted requests
                </p>
              </div>

              <div className="flex gap-2">
                <ActionButton
                  icon={RefreshCw}
                  onClick={fetchMyRequests}
                  className="bg-white text-gray-700 border border-gray-200 hover:bg-gray-50"
                >
                  Refresh
                </ActionButton>

              </div>
            </div>

            {/* ================================================= */}
            {/* EMPTY */}
            {/* ================================================= */}

            {requests.length === 0 ? (
              <div className="bg-white rounded-2xl shadow-xl p-12 text-center">
                <div className="w-14 h-14 mx-auto rounded-xl bg-gray-100 flex items-center justify-center">
                  <FolderOpen
                    size={28}
                    className="text-gray-400"
                  />
                </div>

                <h2 className="text-xl font-bold text-gray-800 mt-4">
                  No requests yet
                </h2>

                <p className="text-gray-500 mt-2">
                  You haven't created any requests.
                </p>

                <ActionButton
                  icon={Plus}
                  onClick={() =>
                    navigate("/requests/create")
                  }
                  className="mt-5 bg-blue-600 text-white hover:bg-blue-700"
                >
                  Create Your First Request
                </ActionButton>
              </div>
            ) : (
              <div className="space-y-4">

                {/* ================================================= */}
                {/* REQUEST CARD */}
                {/* ================================================= */}

                {requests.map((request) => {
                  const category =
                    request.category || "GENERAL";

                  const CategoryIcon =
                    categoryIcons[category] ||
                    FolderOpen;

                  const StatusIcon =
                    statusIcons[request.status] ||
                    Clock3;

                  const categoryStyle =
                    categoryStyles[category] ||
                    categoryStyles.GENERAL;

                  const statusStyle =
                    statusStyles[request.status] ||
                    "bg-gray-100 text-gray-700";

                  return (
                    <article
                      key={request.id}
                      className="
                        group
                        relative
                        bg-white
                        rounded-2xl
                        border border-white/70
                        shadow-xl
                        overflow-hidden
                        transition-all duration-200
                        hover:-translate-y-0.5
                        hover:shadow-2xl
                      "
                    >

                      {/* LEFT ACCENT */}
                      <div className="absolute left-0 top-0 bottom-0 w-1.5 bg-blue-600" />

                      <div className="p-5 sm:p-6 pl-6 sm:pl-7">

                        {/* ================================================= */}
                        {/* HEADER */}
                        {/* ================================================= */}

                        <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-4">

                          <div className="min-w-0">

                            <div className="flex flex-wrap items-center gap-2 mb-2">
                              <span
                                className={`
                                  inline-flex items-center gap-1.5
                                  px-2.5 py-1
                                  rounded-full
                                  text-xs font-semibold
                                  ${categoryStyle}
                                `}
                              >
                                <CategoryIcon size={13} />
                                {category === "FLEET"
                                  ? "Fleet"
                                  : category}
                              </span>

                              <span className="text-xs font-medium text-gray-400">
                                #{request.id}
                              </span>
                            </div>

                            <h2 className="text-lg sm:text-xl font-bold text-gray-800 truncate">
                              {request.title ||
                                "Untitled Request"}
                            </h2>
                          </div>

                          <span
                            className={`
                              inline-flex items-center gap-1.5
                              self-start
                              px-3 py-1.5
                              rounded-full
                              text-xs font-bold
                              ${statusStyle}
                            `}
                          >
                            <StatusIcon size={14} />
                            {request.status}
                          </span>

                        </div>

                        {/* ================================================= */}
                        {/* DESCRIPTION */}
                        {/* ================================================= */}

                        <div className="mt-4">
                          <p className="text-xs font-semibold uppercase tracking-wide text-gray-400 mb-1.5">
                            Description
                          </p>

                          <p className="text-sm sm:text-[15px] text-gray-600 leading-6 line-clamp-2">
                            {request.description ||
                              "No description provided."}
                          </p>
                        </div>

                        {/* ================================================= */}
                        {/* INFO ROW */}
                        {/* ================================================= */}

                        <div className="flex flex-wrap items-center gap-x-6 gap-y-3 mt-5 pt-4 border-t border-gray-100">

                          <div>
                            <p className="text-[11px] uppercase tracking-wide text-gray-400">
                              Category
                            </p>

                            <div className="flex items-center gap-1.5 mt-1">
                              <CategoryIcon
                                size={14}
                                className="text-gray-500"
                              />

                              <span className="text-sm font-semibold text-gray-700">
                                {category === "FLEET"
                                  ? "Fleet"
                                  : category}
                              </span>
                            </div>
                          </div>

                          <div className="h-8 w-px bg-gray-200 hidden sm:block" />

                          <div>
                            <p className="text-[11px] uppercase tracking-wide text-gray-400">
                              Status
                            </p>

                            <div className="flex items-center gap-1.5 mt-1">
                              <StatusIcon
                                size={14}
                                className="text-gray-500"
                              />

                              <span className="text-sm font-semibold text-gray-700">
                                {request.status}
                              </span>
                            </div>
                          </div>

                          <div className="h-8 w-px bg-gray-200 hidden sm:block" />

                          <div>
                            <p className="text-[11px] uppercase tracking-wide text-gray-400">
                              Created
                            </p>

                            <p className="text-sm font-semibold text-gray-700 mt-1">
                              {request.createdAt
                                ? new Date(
                                    request.createdAt
                                  ).toLocaleDateString()
                                : "N/A"}
                            </p>
                          </div>

                        </div>

                        {/* ================================================= */}
                        {/* ACTIONS */}
                        {/* ================================================= */}

                        <div className="flex flex-wrap gap-2 mt-5">

                          {request.status ===
                            "APPROVED" && (
                            <ActionButton
                              icon={Sparkles}
                              onClick={() =>
                                navigate(
                                  `/requests/${request.id}/ai-analysis`
                                )
                              }
                              className="bg-purple-600 text-white hover:bg-purple-700"
                            >
                              AI Analysis
                            </ActionButton>
                          )}

                          <ActionButton
                            icon={Eye}
                            onClick={() =>
                              navigate(
                                `/requests/${request.id}`
                              )
                            }
                            className="bg-gray-800 text-white hover:bg-gray-900"
                          >
                            View
                          </ActionButton>

                          {request.status ===
                            "PENDING" && (
                            <ActionButton
                              icon={Pencil}
                              onClick={() =>
                                navigate(
                                  `/requests/${request.id}/edit`
                                )
                              }
                              className="bg-blue-600 text-white hover:bg-blue-700"
                            >
                              Edit
                            </ActionButton>
                          )}

                          {request.status ===
                            "PENDING" && (
                            <ActionButton
                              icon={Trash2}
                              disabled={
                                deletingId ===
                                request.id
                              }
                              onClick={() =>
                                handleDelete(
                                  request.id
                                )
                              }
                              className="bg-red-50 text-red-600 border border-red-200 hover:bg-red-100"
                            >
                              {deletingId ===
                              request.id
                                ? "Deleting..."
                                : "Delete"}
                            </ActionButton>
                          )}

                        </div>
                      </div>
                    </article>
                  );
                })}
              </div>
            )}

          </div>
        </main>
      </div>
    </div>
  );
}

export default AdminMyRequests;

