import { X } from "lucide-react";

interface Props {
  request: any;
  onClose: () => void;
}

function RequestModal({
  request,
  onClose,
}: Props) {
  const category = request.category || "GENERAL";

  const categoryStyles: Record<string, string> = {
    IT: "bg-cyan-500/20 text-cyan-400",
    LEAVE: "bg-purple-500/20 text-purple-400",
    HR: "bg-orange-500/20 text-orange-400",
    GENERAL: "bg-gray-500/20 text-gray-400",
  };

  return (
    <div
      className="
        fixed
        inset-0
        bg-black/60
        backdrop-blur-sm
        flex
        items-center
        justify-center
        z-[999]
        p-5
      "
      onClick={onClose}
    >

      <div
        className="
          w-full
          max-w-2xl
          bg-[#0f172a]
          border
          border-white/10
          rounded-[32px]
          p-8
          shadow-2xl
          relative
          max-h-[90vh]
          overflow-y-auto
        "
        onClick={(e) =>
          e.stopPropagation()
        }
      >

        {/* CLOSE */}

        <button
          onClick={onClose}
          className="
            absolute
            top-5
            right-5
            bg-white/5
            hover:bg-white/10
            transition
            p-3
            rounded-2xl
          "
        >
          <X size={20} />
        </button>

        {/* HEADER */}

        <div className="mb-8 pr-12">

          <div className="flex items-center gap-3 mb-2">
            <h1
              className="
                text-4xl
                font-black
                text-white
              "
            >
              {request.title ||
                "Request Details"}
            </h1>

            <span
              className={`
                px-3
                py-1
                rounded-lg
                text-xs
                font-bold
                ${categoryStyles[category] || "bg-gray-500/20 text-gray-400"}
              `}
            >
              {category}
            </span>
          </div>

          <p className="text-gray-400 mt-3">
            Full request details
          </p>

        </div>

        {/* DESCRIPTION */}

        <div
          className="
            bg-white/5
            border
            border-white/10
            rounded-3xl
            p-6
          "
        >

          <h2
            className="
              font-bold
              text-lg
              mb-3
            "
          >
            Description
          </h2>

          <p
            className="
              text-gray-300
              leading-8
            "
          >
            {request.description ||
              "No description available"}
          </p>

        </div>

        {/* STATUS */}

        <div
          className="
            flex
            items-center
            justify-between
            bg-white/5
            border
            border-white/10
            rounded-3xl
            p-6
            mt-6
          "
        >

          <div>

            <p className="text-gray-400">
              Request Status
            </p>

            <h2
              className="
                text-2xl
                font-black
                mt-2
              "
            >
              {request.status}
            </h2>

          </div>

          <div
            className={`
              px-5
              py-3
              rounded-2xl
              font-bold

              ${
                request.status === "APPROVED"
                  ? "bg-green-500/20 text-green-400"
                  : request.status === "REJECTED"
                  ? "bg-red-500/20 text-red-400"
                  : "bg-yellow-500/20 text-yellow-300"
              }
            `}
          >
            {request.status}
          </div>

        </div>

        {/* REVIEWED ON */}

        {request.approvedAt && (

          <div
            className="
              bg-white/5
              border
              border-white/10
              rounded-3xl
              p-6
              mt-6
            "
          >

            <p className="text-gray-400">
              {request.status === "REJECTED"
                ? "Rejected On"
                : "Approved On"}
            </p>

            <h2
              className="
                text-xl
                font-bold
                mt-2
              "
            >
              {new Date(request.approvedAt).toLocaleString()}
            </h2>

          </div>

        )}

        {/* INFO */}

        <div
          className="
            grid
            grid-cols-1
            md:grid-cols-2
            gap-5
            mt-6
          "
        >

          {/* ID */}

          <div
            className="
              bg-white/5
              border
              border-white/10
              rounded-3xl
              p-6
            "
          >

            <p className="text-gray-400">
              Request ID
            </p>

            <h2
              className="
                text-2xl
                font-black
                mt-2
              "
            >
              #{request.id}
            </h2>

          </div>

          {/* CREATED BY */}

          <div
            className="
              bg-white/5
              border
              border-white/10
              rounded-3xl
              p-6
            "
          >

            <p className="text-gray-400">
              Created By
            </p>

            <h2
              className="
                text-2xl
                font-black
                mt-2
              "
            >
              {request.user?.name ||
                request.createdBy ||
                "User"}
            </h2>

          </div>

        </div>

      </div>

    </div>
  );
}

export default RequestModal;