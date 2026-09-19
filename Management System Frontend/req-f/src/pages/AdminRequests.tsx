import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import {
  CheckCircle,
  XCircle,
  Sparkles,
} from "lucide-react";

import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import api from "../api/axios";

const CATEGORY_FILTERS = [
  "ALL",
  "GENERAL",
  "IT",
  "LEAVE",
  "HR",
];

function AdminRequests() {
  const navigate = useNavigate();

  const [requests, setRequests] = useState<any[]>(
    []
  );

  const [loading, setLoading] =
    useState(true);

  const [actingId, setActingId] =
    useState<number | null>(null);

  const [categoryFilter, setCategoryFilter] =
    useState("ALL");

  const [selectedIds, setSelectedIds] =
    useState<number[]>([]);

  const [bulkActing, setBulkActing] =
    useState(false);

  // =========================================================
  // FETCH REQUESTS
  // =========================================================

  const fetchRequests = async () => {
    try {
      setLoading(true);

      const res =
        await api.get("/requests");

      setRequests(
        Array.isArray(res.data)
          ? res.data
          : []
      );

      setSelectedIds([]);
    } catch (error) {
      console.log(error);

      toast.error(
        "Failed to fetch requests"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRequests();
  }, []);

  // =========================================================
  // SINGLE APPROVE / REJECT
  // =========================================================

  const handleDecision = async (
    id: number,
    decision: "approve" | "reject"
  ) => {
    setActingId(id);

    try {
      await api.put(
        `/requests/${id}/${decision}`,
        {}
      );

      toast.success(
        decision === "approve"
          ? "Request approved"
          : "Request rejected"
      );

      setRequests((prev) =>
        prev.map((r) =>
          r.id === id
            ? {
                ...r,
                status:
                  decision === "approve"
                    ? "APPROVED"
                    : "REJECTED",
                approvedAt:
                  new Date().toISOString(),
              }
            : r
        )
      );

      setSelectedIds((prev) =>
        prev.filter(
          (selectedId) =>
            selectedId !== id
        )
      );
    } catch (error) {
      console.log(error);

      toast.error("Action failed");
    } finally {
      setActingId(null);
    }
  };

  // =========================================================
  // SELECT / UNSELECT
  // =========================================================

  const toggleSelect = (id: number) => {
    setSelectedIds((prev) =>
      prev.includes(id)
        ? prev.filter(
            (selectedId) =>
              selectedId !== id
          )
        : [...prev, id]
    );
  };

  // =========================================================
  // SELECT ALL PENDING
  // =========================================================

  const toggleSelectAll = () => {
    const pendingIds =
      filteredRequests
        .filter(
          (request) =>
            request.status === "PENDING"
        )
        .map(
          (request) => request.id
        );

    const allSelected =
      pendingIds.length > 0 &&
      pendingIds.every((id) =>
        selectedIds.includes(id)
      );

    if (allSelected) {
      setSelectedIds((prev) =>
        prev.filter(
          (id) =>
            !pendingIds.includes(id)
        )
      );
    } else {
      setSelectedIds((prev) => [
        ...new Set([
          ...prev,
          ...pendingIds,
        ]),
      ]);
    }
  };

  // =========================================================
  // BULK APPROVE / REJECT
  // =========================================================

  const handleBulkDecision = async (
    decision: "approve" | "reject"
  ) => {
    if (selectedIds.length === 0) {
      toast.error(
        "Please select at least one request"
      );
      return;
    }

    const confirmed =
      window.confirm(
        decision === "approve"
          ? `Are you sure you want to approve ${selectedIds.length} request(s)?`
          : `Are you sure you want to reject ${selectedIds.length} request(s)?`
      );

    if (!confirmed) {
      return;
    }

    setBulkActing(true);

    try {
      const results =
        await Promise.allSettled(
          selectedIds.map((id) =>
            api.put(
              `/requests/${id}/${decision}`,
              {}
            )
          )
        );

      const successfulIds: number[] =
        [];

      results.forEach(
        (result, index) => {
          if (
            result.status ===
            "fulfilled"
          ) {
            successfulIds.push(
              selectedIds[index]
            );
          }
        }
      );

      if (
        successfulIds.length === 0
      ) {
        toast.error(
          "No requests were processed"
        );
        return;
      }

      const newStatus =
        decision === "approve"
          ? "APPROVED"
          : "REJECTED";

      setRequests((prev) =>
        prev.map((request) =>
          successfulIds.includes(
            request.id
          )
            ? {
                ...request,
                status: newStatus,
                approvedAt:
                  new Date().toISOString(),
              }
            : request
        )
      );

      setSelectedIds((prev) =>
        prev.filter(
          (id) =>
            !successfulIds.includes(id)
        )
      );

      if (
        successfulIds.length ===
        selectedIds.length
      ) {
        toast.success(
          `${successfulIds.length} request(s) ${
            decision === "approve"
              ? "approved"
              : "rejected"
          } successfully`
        );
      } else {
        toast.success(
          `${successfulIds.length} request(s) processed`
        );

        toast.error(
          `${
            selectedIds.length -
            successfulIds.length
          } request(s) failed`
        );
      }
    } catch (error) {
      console.log(error);

      toast.error(
        "Bulk action failed"
      );
    } finally {
      setBulkActing(false);
    }
  };

  // =========================================================
  // STYLES
  // =========================================================

  const statusStyles: Record<
    string,
    string
  > = {
    APPROVED:
      "bg-green-500/20 text-green-400",

    REJECTED:
      "bg-red-500/20 text-red-400",

    PENDING:
      "bg-yellow-500/20 text-yellow-300",
  };

  const categoryStyles: Record<
    string,
    string
  > = {
    IT:
      "bg-cyan-500/20 text-cyan-400",

    LEAVE:
      "bg-purple-500/20 text-purple-400",

    HR:
      "bg-orange-500/20 text-orange-400",

    GENERAL:
      "bg-gray-500/20 text-gray-400",
  };

  // =========================================================
  // FILTER
  // =========================================================

  const filteredRequests =
    categoryFilter === "ALL"
      ? requests
      : requests.filter(
          (r) =>
            (r.category ||
              "GENERAL") ===
            categoryFilter
        );

  // =========================================================
  // SELECTION
  // =========================================================

  const pendingFilteredIds =
    filteredRequests
      .filter(
        (request) =>
          request.status ===
          "PENDING"
      )
      .map(
        (request) =>
          request.id
      );

  const allPendingSelected =
    pendingFilteredIds.length >
      0 &&
    pendingFilteredIds.every(
      (id) =>
        selectedIds.includes(id)
    );

  const someSelected =
    selectedIds.length > 0;

  // =========================================================
  // LOADING
  // =========================================================

  if (loading) {
    return (
      <div
        className="
          min-h-screen
          bg-[#020617]
          flex
          items-center
          justify-center
        "
      >
        <div
          className="
            w-16
            h-16
            border-4
            border-blue-500
            border-t-transparent
            rounded-full
            animate-spin
          "
        />
      </div>
    );
  }

  // =========================================================
  // PAGE
  // =========================================================

  return (
    <div
      className="
        flex
        bg-gradient-to-br
        from-[#020617]
        via-[#07122b]
        to-[#020617]
        min-h-screen
        text-white
      "
    >
      {/* SIDEBAR */}
      <Sidebar />

      {/* MAIN */}
      <div className="flex-1">

        {/* NAVBAR */}
        <Navbar />

        {/* CONTENT */}
        <div className="p-8">

          {/* HEADER */}
          <div className="mb-10">
            <h1 className="text-5xl font-black tracking-tight">
              Admin Requests
            </h1>

            <p className="text-gray-400 mt-2 text-lg">
              Review and act on submitted requests
            </p>
          </div>

          {/* CATEGORY FILTER */}
          <div className="flex flex-wrap gap-2 mb-6">
            {CATEGORY_FILTERS.map(
              (cat) => (
                <button
                  key={cat}
                  onClick={() =>
                    setCategoryFilter(
                      cat
                    )
                  }
                  className={`
                    px-4
                    py-2
                    rounded-2xl
                    text-sm
                    font-semibold
                    transition
                    ${
                      categoryFilter ===
                      cat
                        ? "bg-blue-500 text-white"
                        : "bg-white/5 border border-white/10 text-gray-400 hover:bg-white/10"
                    }
                  `}
                >
                  {cat === "ALL"
                    ? "All"
                    : cat}
                </button>
              )
            )}
          </div>

          {/* BULK ACTION BAR */}
          <div
            className="
              mb-6
              bg-white/5
              border
              border-white/10
              rounded-[28px]
              p-5
              backdrop-blur-2xl
              flex
              flex-wrap
              items-center
              justify-between
              gap-4
            "
          >
            <div className="flex items-center gap-4">

              <label
                className="
                  flex
                  items-center
                  gap-3
                  cursor-pointer
                  text-gray-300
                  font-semibold
                "
              >
                <input
                  type="checkbox"
                  checked={
                    allPendingSelected
                  }
                  onChange={
                    toggleSelectAll
                  }
                  disabled={
                    pendingFilteredIds.length ===
                      0 ||
                    bulkActing
                  }
                  className="
                    w-5
                    h-5
                    accent-blue-500
                    cursor-pointer
                  "
                />

                Select All Pending
              </label>

              <span className="text-gray-500">
                {selectedIds.length}{" "}
                selected
              </span>
            </div>

            {someSelected && (
              <div className="flex gap-3">

                <button
                  onClick={() =>
                    handleBulkDecision(
                      "approve"
                    )
                  }
                  disabled={
                    bulkActing
                  }
                  className="
                    bg-green-500
                    hover:bg-green-600
                    disabled:opacity-50
                    disabled:cursor-not-allowed
                    px-5
                    py-2.5
                    rounded-2xl
                    font-semibold
                    transition
                  "
                >
                  {bulkActing
                    ? "Processing..."
                    : "Approve Selected"}
                </button>

                <button
                  onClick={() =>
                    handleBulkDecision(
                      "reject"
                    )
                  }
                  disabled={
                    bulkActing
                  }
                  className="
                    bg-red-500
                    hover:bg-red-600
                    disabled:opacity-50
                    disabled:cursor-not-allowed
                    px-5
                    py-2.5
                    rounded-2xl
                    font-semibold
                    transition
                  "
                >
                  {bulkActing
                    ? "Processing..."
                    : "Reject Selected"}
                </button>

              </div>
            )}
          </div>

          {/* REQUESTS */}
          <div
            className="
              bg-white/5
              border
              border-white/10
              backdrop-blur-2xl
              rounded-[40px]
              p-8
              shadow-2xl
            "
          >
            {filteredRequests.length ===
            0 ? (
              <div
                className="
                  text-center
                  py-20
                  text-gray-400
                "
              >
                <h2 className="text-2xl font-bold">
                  No Requests Found
                </h2>
              </div>
            ) : (
              <div className="flex flex-col gap-5">

                {filteredRequests.map(
                  (request) => {

                    const category =
                      request.category ||
                      "GENERAL";

                    const isPending =
                      request.status ===
                      "PENDING";

                    const isApproved =
                      request.status ===
                      "APPROVED";

                    return (
                      <div
                        key={request.id}
                        className="
                          bg-white/5
                          border
                          border-white/10
                          rounded-[28px]
                          p-6
                        "
                      >

                        {/* TOP */}
                        <div className="flex items-center justify-between gap-5">

                          <div className="flex items-start gap-4">

                            {/* CHECKBOX */}
                            {isPending && (
                              <input
                                type="checkbox"
                                checked={selectedIds.includes(
                                  request.id
                                )}
                                onChange={() =>
                                  toggleSelect(
                                    request.id
                                  )
                                }
                                disabled={
                                  bulkActing
                                }
                                className="
                                  w-5
                                  h-5
                                  mt-2
                                  accent-blue-500
                                  cursor-pointer
                                "
                              />
                            )}

                            <div>

                              <div className="flex flex-wrap items-center gap-3 mb-2">

                                <h2 className="text-2xl font-bold">
                                  {request.title ||
                                    "Untitled Request"}
                                </h2>

                                <span
                                  className={`
                                    px-3
                                    py-1
                                    rounded-lg
                                    text-xs
                                    font-bold
                                    ${
                                      categoryStyles[
                                        category
                                      ] ||
                                      "bg-gray-500/20 text-gray-400"
                                    }
                                  `}
                                >
                                  {category}
                                </span>

                              </div>

                              <p className="text-gray-400 mt-2">
                                {request.description ||
                                  "No description"}
                              </p>

                              {request.user?.name && (
                                <p className="text-gray-500 text-sm mt-2">
                                  Submitted by{" "}
                                  {
                                    request.user
                                      .name
                                  }
                                </p>
                              )}

                              {request.approvedAt && (
                                <p className="text-gray-500 text-xs mt-1">
                                  {request.status ===
                                  "REJECTED"
                                    ? "Rejected"
                                    : "Approved"}{" "}
                                  on{" "}
                                  {new Date(
                                    request.approvedAt
                                  ).toLocaleString()}
                                </p>
                              )}

                            </div>
                          </div>

                          {/* STATUS */}
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
                                "bg-white/10 text-gray-300"
                              }
                            `}
                          >
                            {request.status}
                          </span>

                        </div>

                        {/* ================================================= */}
                        {/* ACTIONS */}
                        {/* ================================================= */}

                        <div className="flex flex-wrap gap-3 mt-6 ml-9">

                          {/* APPROVE */}
                          {isPending && (
                            <button
                              onClick={() =>
                                handleDecision(
                                  request.id,
                                  "approve"
                                )
                              }
                              disabled={
                                actingId ===
                                  request.id ||
                                bulkActing
                              }
                              className="
                                flex
                                items-center
                                gap-2
                                bg-green-500
                                hover:bg-green-600
                                disabled:opacity-50
                                disabled:cursor-not-allowed
                                px-5
                                py-2.5
                                rounded-2xl
                                font-semibold
                                transition
                              "
                            >
                              <CheckCircle
                                size={17}
                              />

                              {actingId ===
                              request.id
                                ? "Processing..."
                                : "Approve"}
                            </button>
                          )}

                          {/* REJECT */}
                          {isPending && (
                            <button
                              onClick={() =>
                                handleDecision(
                                  request.id,
                                  "reject"
                                )
                              }
                              disabled={
                                actingId ===
                                  request.id ||
                                bulkActing
                              }
                              className="
                                flex
                                items-center
                                gap-2
                                bg-red-500
                                hover:bg-red-600
                                disabled:opacity-50
                                disabled:cursor-not-allowed
                                px-5
                                py-2.5
                                rounded-2xl
                                font-semibold
                                transition
                              "
                            >
                              <XCircle
                                size={17}
                              />

                              {actingId ===
                              request.id
                                ? "Processing..."
                                : "Reject"}
                            </button>
                          )}

                          {/* ================================================= */}
                          {/* ADMIN AI ANALYSIS */}
                          {/* ================================================= */}

                          {isApproved && (
                            <button
                              type="button"
                              onClick={() =>
                                navigate(
                                  `/requests/${request.id}/ai-analysis/admin`
                                )
                              }
                              className="
                                flex
                                items-center
                                gap-2
                                bg-purple-600
                                hover:bg-purple-700
                                px-5
                                py-2.5
                                rounded-2xl
                                font-semibold
                                transition
                              "
                            >
                              <Sparkles
                                size={17}
                              />

                              AI Analysis
                            </button>
                          )}

                        </div>
                      </div>
                    );
                  }
                )}

              </div>
            )}
          </div>

        </div>
      </div>
    </div>
  );
}

export default AdminRequests;