
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  FileText,
  Plus,
  Eye,
  Pencil,
  Trash2,
  ShieldCheck,
  Loader2,
} from "lucide-react";
import toast from "react-hot-toast";

import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import api from "../api/axios";

const BG = "rgb(2, 6, 23)";

const categoryStyles = {
  IT: "bg-cyan-500/20 text-cyan-400",
  FLEET: "bg-blue-500/20 text-blue-400",
  LEAVE: "bg-purple-500/20 text-purple-400",
  HR: "bg-orange-500/20 text-orange-400",
  GENERAL: "bg-gray-500/20 text-gray-400",
};

const statusStyles = {
  APPROVED: "bg-green-500/20 text-green-400",
  REJECTED: "bg-red-500/20 text-red-400",
  PENDING: "bg-yellow-500/20 text-yellow-400",
};

function ActionButton({
  icon,
  children,
  onClick,
  disabled = false,
  className = "",
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      className={`
        flex
        items-center
        gap-2
        px-4
        py-2.5
        rounded-2xl
        font-semibold
        transition
        disabled:opacity-50
        disabled:cursor-not-allowed
        ${className}
      `}
    >
      {icon}
      {children}
    </button>
  );
}

function MyRequests() {
  const navigate = useNavigate();

  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [deletingId, setDeletingId] = useState(null);
  const [selectedCategory, setSelectedCategory] =
    useState("ALL");

  const loadRequests = async () => {
    try {
      setLoading(true);

      const response = await api.get("/requests/my");

      setRequests(response.data);
    } catch (err) {
      console.error(err);

      toast.error(
        err.response?.data?.message ||
          "Failed to load requests"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRequests();
  }, []);

  const filteredRequests = useMemo(() => {
    if (selectedCategory === "ALL") {
      return requests;
    }

    return requests.filter(
      (request) =>
        request.category === selectedCategory
    );
  }, [requests, selectedCategory]);

  const deleteRequest = async (id) => {
    const confirmed = window.confirm(
      "Are you sure you want to delete this request?"
    );

    if (!confirmed) {
      return;
    }

    try {
      setDeletingId(id);

      await api.delete(`/requests/${id}`);

      setRequests((current) =>
        current.filter(
          (request) => request.id !== id
        )
      );

      toast.success(
        "Request deleted successfully"
      );
    } catch (err) {
      console.error(err);

      toast.error(
        err.response?.data?.message ||
          "Failed to delete request"
      );
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <div
      className="flex min-h-screen text-white"
      style={{ backgroundColor: BG }}
    >
      <Sidebar />

      <div className="flex-1 min-w-0">
        <Navbar />

        <main className="p-4 sm:p-6 lg:p-8">
          <div className="max-w-6xl mx-auto">

            {/* HEADER */}
            <div
              className="
                flex
                flex-col
                sm:flex-row
                sm:items-center
                sm:justify-between
                gap-4
                mb-8
              "
            >
              <div>
                <h1 className="text-3xl font-bold">
                  My Requests
                </h1>

                <p className="text-slate-400 mt-1">
                  Manage and track your requests
                </p>
              </div>

              <button
                type="button"
                onClick={() => navigate("/create")}
                className="
                  inline-flex
                  items-center
                  justify-center
                  gap-2
                  bg-blue-600
                  hover:bg-blue-700
                  px-5
                  py-3
                  rounded-2xl
                  font-semibold
                  transition
                "
              >
                <Plus size={19} />
                Create Request
              </button>
            </div>

            {/* FILTERS */}
            <div className="flex flex-wrap gap-3 mb-7">
              {[
                "ALL",
                "GENERAL",
                "IT",
                "FLEET",
                "LEAVE",
                "HR",
              ].map((category) => {
                const active =
                  selectedCategory === category;

                return (
                  <button
                    key={category}
                    type="button"
                    onClick={() =>
                      setSelectedCategory(category)
                    }
                    className={`
                      px-4
                      py-2
                      rounded-2xl
                      text-sm
                      font-semibold
                      transition
                      border
                      ${
                        active
                          ? "bg-blue-600 border-blue-500 text-white"
                          : "bg-white/5 border-white/10 text-slate-400 hover:text-white hover:bg-white/10"
                      }
                    `}
                  >
                    {category}
                  </button>
                );
              })}
            </div>

            {/* LOADING */}
            {loading && (
              <div className="flex justify-center py-20">
                <Loader2
                  size={32}
                  className="
                    animate-spin
                    text-blue-400
                  "
                />
              </div>
            )}

            {/* EMPTY */}
            {!loading &&
              filteredRequests.length === 0 && (
                <div
                  className="
                    bg-white/5
                    border border-white/10
                    rounded-[28px]
                    p-10
                    text-center
                  "
                >
                  <FileText
                    size={42}
                    className="
                      mx-auto
                      text-slate-500
                    "
                  />

                  <h2 className="
                    text-xl
                    font-semibold
                    mt-4
                  ">
                    No requests found
                  </h2>

                  <p className="
                    text-slate-400
                    mt-2
                  ">
                    You don't have any requests
                    in this category.
                  </p>

                  <button
                    type="button"
                    onClick={() =>
                      navigate("/create")
                    }
                    className="
                      mt-6
                      inline-flex
                      items-center
                      gap-2
                      bg-blue-600
                      hover:bg-blue-700
                      px-5
                      py-3
                      rounded-2xl
                      font-semibold
                      transition
                    "
                  >
                    <Plus size={18} />
                    Create Request
                  </button>
                </div>
              )}

            {/* REQUESTS */}
            {!loading &&
              filteredRequests.length > 0 && (
                <div className="space-y-5">

                  {filteredRequests.map(
                    (request) => (
                      <div
                        key={request.id}
                        className="
                          bg-white/5
                          border border-white/10
                          rounded-[28px]
                          p-6
                          hover:bg-white/[0.07]
                          hover:border-white/15
                          transition
                        "
                      >
                        {/* TOP */}
                        <div className="
                          flex
                          flex-col
                          lg:flex-row
                          lg:items-start
                          lg:justify-between
                          gap-5
                        ">

                          {/* INFO */}
                          <div className="min-w-0">
                            <div className="
                              flex
                              flex-wrap
                              items-center
                              gap-3
                            ">
                              <span
                                className={`
                                  px-4
                                  py-2
                                  rounded-2xl
                                  font-semibold
                                  whitespace-nowrap
                                  ${
                                    statusStyles[
                                      request.status
                                    ] ||
                                    "bg-white/10 text-slate-400"
                                  }
                                `}
                              >
                                {request.status}
                              </span>

                              {request.category && (
                                <span
                                  className={`
                                    px-4
                                    py-2
                                    rounded-2xl
                                    font-semibold
                                    whitespace-nowrap
                                    ${
                                      categoryStyles[
                                        request.category
                                      ] ||
                                      "bg-white/10 text-slate-400"
                                    }
                                  `}
                                >
                                  {request.category}
                                </span>
                              )}

                              <span className="
                                text-sm
                                text-slate-500
                              ">
                                #{request.id}
                              </span>
                            </div>

                            <h2 className="
                              text-xl
                              font-bold
                              mt-4
                              break-words
                            ">
                              {request.title}
                            </h2>

                            <p className="
                              text-slate-400
                              mt-2
                              leading-6
                              break-words
                            ">
                              {request.description}
                            </p>

                            {request.createdAt && (
                              <p className="
                                text-sm
                                text-slate-500
                                mt-4
                              ">
                                Created{" "}
                                {new Date(
                                  request.createdAt
                                ).toLocaleString()}
                              </p>
                            )}
                          </div>

                          {/* ACTIONS */}
                          <div className="
                            flex
                            flex-wrap
                            gap-3
                            shrink-0
                          ">

                            {/* WORKFLOW */}
                            <ActionButton
                              icon={
                                <ShieldCheck
                                  size={18}
                                />
                              }
                              onClick={() =>
                                navigate(
                                  `/requests/${request.id}/workflow`
                                )
                              }
                              className="
                                bg-blue-600
                                hover:bg-blue-700
                                text-white
                              "
                            >
                              Workflow
                            </ActionButton>

                            {/* AI ANALYSIS */}
                            {request.status ===
                              "APPROVED" && (
                              <ActionButton
                                onClick={() =>
                                  navigate(
                                    `/requests/${request.id}/ai-analysis`
                                  )
                                }
                                className="
                                  bg-purple-600
                                  hover:bg-purple-700
                                  text-white
                                "
                              >
                                AI Analysis
                              </ActionButton>
                            )}

                            {/* VIEW */}
                            <ActionButton
                              icon={
                                <Eye size={18} />
                              }
                              onClick={() =>
                                navigate(
                                  `/requests/my/${request.id}`
                                )
                              }
                              className="
                                bg-white/5
                                border
                                border-white/10
                                text-slate-300
                                hover:bg-white/10
                                hover:text-white
                              "
                            >
                              View
                            </ActionButton>

                            {/* EDIT */}
                            {request.status ===
                              "PENDING" && (
                              <ActionButton
                                icon={
                                  <Pencil size={18} />
                                }
                                onClick={() =>
                                  navigate(
                                    `/requests/${request.id}/edit`
                                  )
                                }
                                className="
                                  bg-white/5
                                  border
                                  border-white/10
                                  text-slate-300
                                  hover:bg-white/10
                                  hover:text-white
                                "
                              >
                                Edit
                              </ActionButton>
                            )}

                            {/* DELETE */}
                            {request.status ===
                              "PENDING" && (
                              <ActionButton
                                icon={
                                  deletingId ===
                                  request.id ? (
                                    <Loader2
                                      size={18}
                                      className="
                                        animate-spin
                                      "
                                    />
                                  ) : (
                                    <Trash2 size={18} />
                                  )
                                }
                                disabled={
                                  deletingId ===
                                  request.id
                                }
                                onClick={() =>
                                  deleteRequest(
                                    request.id
                                  )
                                }
                                className="
                                  bg-red-500/10
                                  border
                                  border-red-500/20
                                  text-red-400
                                  hover:bg-red-500/20
                                "
                              >
                                Delete
                              </ActionButton>
                            )}
                          </div>
                        </div>
                      </div>
                    )
                  )}

                </div>
              )}

          </div>
        </main>
      </div>
    </div>
  );
}

export default MyRequests;
