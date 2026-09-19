
import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  CheckCircle2,
  Clock3,
  XCircle,
  Loader2,
  ShieldCheck,
  UserCircle,
} from "lucide-react";
import toast from "react-hot-toast";

import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import api from "../api/axios";

const BG = "rgb(2, 6, 23)";

function RequestWorkflow() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [workflow, setWorkflow] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadWorkflow = async () => {
    try {
      setLoading(true);

      const response = await api.get(
        `/requests/${id}/workflow`
      );

      setWorkflow(response.data);
    } catch (err) {
      console.error(err);

      toast.error(
        err.response?.data?.message ||
          "Failed to load request workflow"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadWorkflow();
  }, [id]);

  const getStatusStyle = (status) => {
    switch (status) {
      case "APPROVED":
        return {
          bg: "bg-green-500/10",
          border: "border-green-500/20",
          text: "text-green-400",
          icon: <CheckCircle2 size={20} />,
        };

      case "REJECTED":
        return {
          bg: "bg-red-500/10",
          border: "border-red-500/20",
          text: "text-red-400",
          icon: <XCircle size={20} />,
        };

      case "PENDING":
        return {
          bg: "bg-yellow-500/10",
          border: "border-yellow-500/20",
          text: "text-yellow-400",
          icon: <Clock3 size={20} />,
        };

      default:
        return {
          bg: "bg-white/5",
          border: "border-white/10",
          text: "text-slate-400",
          icon: <Clock3 size={20} />,
        };
    }
  };

  const getStatusLabel = (status) => {
    switch (status) {
      case "APPROVED":
        return "Approved";

      case "REJECTED":
        return "Rejected";

      case "PENDING":
        return "Waiting for approval";

      case "WAITING":
        return "Waiting";

      default:
        return status;
    }
  };

  const getRoleLabel = (role) => {
    switch (role) {
      case "ADMIN":
        return "Administrator";

      case "MANAGER":
        return "Manager";

      default:
        return role;
    }
  };

  if (loading) {
    return (
      <div
        className="flex min-h-screen text-white"
        style={{ backgroundColor: BG }}
      >
        <Sidebar />

        <div className="flex-1 min-w-0">
          <Navbar />

          <div className="flex justify-center items-center h-[70vh]">
            <Loader2
              size={30}
              className="animate-spin text-blue-400"
            />
          </div>
        </div>
      </div>
    );
  }

  if (!workflow) {
    return (
      <div
        className="flex min-h-screen text-white"
        style={{ backgroundColor: BG }}
      >
        <Sidebar />

        <div className="flex-1 min-w-0">
          <Navbar />

          <main className="p-6 lg:p-8">
            <div className="max-w-4xl mx-auto text-center py-20">
              <h1 className="text-2xl font-bold">
                Workflow not found
              </h1>

              <button
                onClick={() => navigate("/my-requests")}
                className="
                  mt-6
                  px-5 py-3
                  rounded-2xl
                  bg-blue-600
                  hover:bg-blue-700
                  transition
                  font-semibold
                "
              >
                Back to My Requests
              </button>
            </div>
          </main>
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
          <div className="max-w-4xl mx-auto">

            {/* HEADER */}
            <div className="mb-8">
              <button
                onClick={() => navigate("/my-requests")}
                className="
                  inline-flex
                  items-center
                  gap-2
                  text-slate-400
                  hover:text-white
                  transition
                  mb-5
                "
              >
                <ArrowLeft size={18} />
                Back to My Requests
              </button>

              <h1 className="text-3xl font-bold">
                Request Workflow
              </h1>

              <p className="text-slate-400 mt-1">
                Track who is reviewing your request
              </p>
            </div>

            {/* CURRENT STEP */}
            <div
              className="
                bg-white/5
                border border-white/10
                rounded-[28px]
                p-6 sm:p-8
                mb-6
              "
            >
              <div className="flex items-start gap-4">
                <div
                  className="
                    w-12 h-12
                    rounded-2xl
                    bg-blue-500/10
                    border border-blue-500/20
                    flex items-center justify-center
                    shrink-0
                  "
                >
                  <ShieldCheck
                    size={24}
                    className="text-blue-400"
                  />
                </div>

                <div>
                  <p className="text-sm text-slate-500">
                    Current Step
                  </p>

                  <h2 className="text-xl font-bold mt-1">
                    {workflow.currentStep}
                  </h2>

                  {workflow.currentApproverName && (
                    <div className="flex items-center gap-2 mt-3 text-slate-300">
                      <UserCircle size={18} />

                      <span>
                        Waiting for{" "}
                        <span className="font-semibold text-white">
                          {workflow.currentApproverName}
                        </span>
                      </span>
                    </div>
                  )}

                  {workflow.currentApproverRole && (
                    <p className="text-sm text-slate-500 mt-1">
                      {getRoleLabel(
                        workflow.currentApproverRole
                      )}
                    </p>
                  )}
                </div>
              </div>
            </div>

            {/* TIMELINE */}
            <div
              className="
                bg-white/5
                border border-white/10
                rounded-[28px]
                p-6 sm:p-8
              "
            >
              <h2 className="text-xl font-bold mb-8">
                Approval Timeline
              </h2>

              <div className="space-y-8">
                {workflow.steps.map((step, index) => {
                  const style =
                    getStatusStyle(step.status);

                  const isLast =
                    index ===
                    workflow.steps.length - 1;

                  return (
                    <div
                      key={step.stepOrder}
                      className="relative flex gap-4"
                    >
                      {/* CONNECTING LINE */}
                      {!isLast && (
                        <div
                          className="
                            absolute
                            left-[19px]
                            top-10
                            w-px
                            h-[calc(100%+2rem)]
                            bg-white/10
                          "
                        />
                      )}

                      {/* ICON */}
                      <div
                        className={`
                          relative
                          z-10
                          w-10 h-10
                          rounded-xl
                          flex items-center justify-center
                          border
                          shrink-0
                          ${style.bg}
                          ${style.border}
                          ${style.text}
                        `}
                      >
                        {style.icon}
                      </div>

                      {/* CONTENT */}
                      <div
                        className={`
                          flex-1
                          rounded-2xl
                          border
                          p-5
                          ${style.bg}
                          ${style.border}
                        `}
                      >
                        <div
                          className="
                            flex
                            flex-col
                            sm:flex-row
                            sm:items-center
                            sm:justify-between
                            gap-3
                          "
                        >
                          <div>
                            <p className="text-sm text-slate-500">
                              Step {step.stepOrder}
                            </p>

                            <h3 className="text-lg font-bold mt-1">
                              {getRoleLabel(
                                step.requiredRole
                              )} Approval
                            </h3>
                          </div>

                          <span
                            className={`
                              inline-flex
                              items-center
                              px-3
                              py-1.5
                              rounded-xl
                              text-sm
                              font-semibold
                              w-fit
                              ${style.bg}
                              ${style.text}
                            `}
                          >
                            {getStatusLabel(
                              step.status
                            )}
                          </span>
                        </div>

                        {step.approverName && (
                          <div className="
                            flex
                            items-center
                            gap-2
                            mt-4
                            text-slate-400
                          ">
                            <UserCircle size={17} />

                            <span>
                              {step.status === "PENDING"
                                ? "Assigned to"
                                : "Handled by"}{" "}
                              <span className="text-white font-medium">
                                {step.approverName}
                              </span>
                            </span>
                          </div>
                        )}

                        {step.actionDate && (
                          <p className="
                            text-sm
                            text-slate-500
                            mt-3
                          ">
                            {new Date(
                              step.actionDate
                            ).toLocaleString()}
                          </p>
                        )}

                        {step.comment && (
                          <div className="
                            mt-4
                            px-4 py-3
                            rounded-xl
                            bg-black/10
                            border
                            border-white/5
                          ">
                            <p className="text-xs text-slate-500 mb-1">
                              Comment
                            </p>

                            <p className="text-sm text-slate-300">
                              {step.comment}
                            </p>
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

          </div>
        </main>
      </div>
    </div>
  );
}

export default RequestWorkflow;

