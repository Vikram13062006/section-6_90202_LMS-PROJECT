import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import Layout from "@components/layout/Layout";
import {
  FaCheckCircle,
  FaClipboardList,
  FaClock,
  FaFileAlt,
  FaPlus,
  FaSearch,
  FaStar,
} from "react-icons/fa";
import { getCurrentUser } from "@utils/auth";
import {
  createAssignment,
  getAssignments,
  getCourses,
  getSubmissions,
  gradeSubmission,
  saveAssignment,
  saveSubmission,
  submitAssignment,
} from "@utils/courses";
import "./Assignments.css";

const Assignments = () => {
  const navigate = useNavigate();
  const currentUser = useMemo(() => getCurrentUser(), []);
  const role = currentUser?.role || "student";

  const [assignments, setAssignments] = useState([]);
  const [courses, setCourses] = useState([]);
  const [submissions, setSubmissions] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCourse, setSelectedCourse] = useState("All");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const [showAssignmentForm, setShowAssignmentForm] = useState(false);
  const [editingAssignmentId, setEditingAssignmentId] = useState("");
  const [assignmentForm, setAssignmentForm] = useState({
    title: "",
    description: "",
    courseId: "",
    dueDate: "",
    points: 100,
    status: "Active",
  });

  const [gradingSubmissionId, setGradingSubmissionId] = useState("");
  const [gradingForm, setGradingForm] = useState({ grade: "", feedback: "" });

  const canManageAssignments = role === "teacher" || role === "admin";

  const loadData = () => {
    setAssignments(getAssignments());
    setCourses(getCourses());
    setSubmissions(getSubmissions());
  };

  useEffect(() => {
    try {
      setLoading(true);
      loadData();
    } catch {
      setError("Failed to load assignments");
    } finally {
      setLoading(false);
    }
  }, []);

  const clearMessages = () => {
    setError("");
    setMessage("");
  };

  const filteredAssignments = useMemo(() => {
    return assignments.filter((item) => {
      const course = courses.find((c) => c.id === item.courseId);
      const search = searchTerm.trim().toLowerCase();
      const searchMatch =
        !search ||
        item.title?.toLowerCase().includes(search) ||
        item.description?.toLowerCase().includes(search) ||
        course?.title?.toLowerCase().includes(search);

      const courseMatch = selectedCourse === "All" || item.courseId === selectedCourse;
      return searchMatch && courseMatch;
    });
  }, [assignments, courses, searchTerm, selectedCourse]);

  const getMySubmission = (assignmentId) => {
    return submissions.find(
      (s) => s.assignmentId === assignmentId && s.studentId === currentUser?.id
    );
  };

  const getSubmissionCount = (assignmentId) => {
    return submissions.filter((s) => s.assignmentId === assignmentId).length;
  };

  const resetAssignmentForm = () => {
    setAssignmentForm({
      title: "",
      description: "",
      courseId: "",
      dueDate: "",
      points: 100,
      status: "Active",
    });
    setEditingAssignmentId("");
  };

  const openCreateAssignment = () => {
    clearMessages();
    resetAssignmentForm();
    setShowAssignmentForm(true);
  };

  const openEditAssignment = (assignment) => {
    clearMessages();
    setEditingAssignmentId(assignment.id);
    setAssignmentForm({
      title: assignment.title || "",
      description: assignment.description || "",
      courseId: assignment.courseId || "",
      dueDate: assignment.dueDate ? assignment.dueDate.slice(0, 10) : "",
      points: assignment.points || 100,
      status: assignment.status || "Active",
    });
    setShowAssignmentForm(true);
  };

  const handleSaveAssignment = () => {
    if (
      !assignmentForm.title.trim() ||
      !assignmentForm.description.trim() ||
      !assignmentForm.courseId ||
      !assignmentForm.dueDate
    ) {
      setError("Title, description, course, and due date are required");
      return;
    }

    const payload = createAssignment({
      ...(editingAssignmentId ? { id: editingAssignmentId } : {}),
      title: assignmentForm.title.trim(),
      description: assignmentForm.description.trim(),
      courseId: assignmentForm.courseId,
      dueDate: new Date(assignmentForm.dueDate).toISOString(),
      points: Number(assignmentForm.points || 100),
      status: assignmentForm.status,
      createdAt: editingAssignmentId
        ? assignments.find((a) => a.id === editingAssignmentId)?.createdAt || new Date().toISOString()
        : new Date().toISOString(),
    });

    const saved = saveAssignment(payload);
    if (!saved) {
      setError("Failed to save assignment");
      return;
    }

    loadData();
    setShowAssignmentForm(false);
    resetAssignmentForm();
    setMessage(editingAssignmentId ? "Assignment updated" : "Assignment created");
  };

  const handleSubmit = (assignment) => {
    const existing = getMySubmission(assignment.id);
    if (existing) {
      setError("You already submitted this assignment");
      return;
    }

    const submission = submitAssignment({
      assignmentId: assignment.id,
      studentId: currentUser?.id || "",
      studentName: currentUser?.name || currentUser?.email || "Student",
      content: `Submission by ${currentUser?.name || "Student"}`,
      status: "submitted",
    });

    const saved = saveSubmission(submission);
    if (!saved) {
      setError("Submission failed");
      return;
    }

    loadData();
    setMessage("Assignment submitted successfully");
  };

  const openGrade = (submissionId) => {
    setGradingSubmissionId(submissionId);
    setGradingForm({ grade: "", feedback: "" });
    clearMessages();
  };

  const handleGrade = () => {
    if (!gradingSubmissionId) return;
    if (gradingForm.grade === "") {
      setError("Grade is required");
      return;
    }

    const graded = gradeSubmission(
      gradingSubmissionId,
      Number(gradingForm.grade),
      gradingForm.feedback.trim()
    );

    if (!graded) {
      setError("Failed to grade submission");
      return;
    }

    loadData();
    setGradingSubmissionId("");
    setGradingForm({ grade: "", feedback: "" });
    setMessage("Submission graded");
  };

  if (loading) {
    return (
      <Layout>
        <div className="assignments-page">
          <div className="loading-state">
            <div className="spinner"></div>
            <p>Loading assignments...</p>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="assignments-page">
        <div className="assignments-header">
          <div>
            <h1>Assignments</h1>
            <p>{filteredAssignments.length} assignment(s)</p>
          </div>
          {canManageAssignments && (
            <button className="btn btn-primary" onClick={openCreateAssignment}>
              <FaPlus /> New Assignment
            </button>
          )}
        </div>

        {error && <div className="alert alert-danger">{error}</div>}
        {message && <div className="alert alert-success">{message}</div>}

        {showAssignmentForm && (
          <section className="panel">
            <h3>{editingAssignmentId ? "Edit Assignment" : "Create Assignment"}</h3>
            <div className="form-grid">
              <input
                className="input"
                placeholder="Title"
                value={assignmentForm.title}
                onChange={(e) =>
                  setAssignmentForm((prev) => ({ ...prev, title: e.target.value }))
                }
              />
              <select
                className="input"
                value={assignmentForm.courseId}
                onChange={(e) =>
                  setAssignmentForm((prev) => ({ ...prev, courseId: e.target.value }))
                }
              >
                <option value="">Select Course</option>
                {courses.map((course) => (
                  <option key={course.id} value={course.id}>
                    {course.title}
                  </option>
                ))}
              </select>
              <input
                className="input"
                type="date"
                value={assignmentForm.dueDate}
                onChange={(e) =>
                  setAssignmentForm((prev) => ({ ...prev, dueDate: e.target.value }))
                }
              />
              <input
                className="input"
                type="number"
                min="1"
                value={assignmentForm.points}
                onChange={(e) =>
                  setAssignmentForm((prev) => ({ ...prev, points: e.target.value }))
                }
              />
              <textarea
                className="input textarea full"
                placeholder="Description"
                value={assignmentForm.description}
                onChange={(e) =>
                  setAssignmentForm((prev) => ({ ...prev, description: e.target.value }))
                }
              />
            </div>
            <div className="row-actions">
              <button className="btn btn-primary" onClick={handleSaveAssignment}>Save</button>
              <button
                className="btn btn-muted"
                onClick={() => {
                  setShowAssignmentForm(false);
                  resetAssignmentForm();
                }}
              >
                Cancel
              </button>
            </div>
          </section>
        )}

        <section className="panel filter-bar">
          <div className="search-wrap">
            <FaSearch />
            <input
              className="input"
              placeholder="Search assignments"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <select
            className="input"
            value={selectedCourse}
            onChange={(e) => setSelectedCourse(e.target.value)}
          >
            <option value="All">All Courses</option>
            {courses.map((course) => (
              <option key={course.id} value={course.id}>
                {course.title}
              </option>
            ))}
          </select>
        </section>

        <section className="assignment-list">
          {filteredAssignments.length === 0 ? (
            <div className="panel empty-state">
              <FaClipboardList className="empty-icon" />
              <p>No assignments found.</p>
            </div>
          ) : (
            filteredAssignments.map((assignment) => {
              const course = courses.find((c) => c.id === assignment.courseId);
              const mySubmission = getMySubmission(assignment.id);
              const count = getSubmissionCount(assignment.id);
              const isOverdue = new Date(assignment.dueDate) < new Date();

              return (
                <article className="panel assignment-card" key={assignment.id}>
                  <div className="card-top">
                    <h3>{assignment.title}</h3>
                    <span className={`status ${isOverdue ? "overdue" : "active"}`}>
                      <FaClock /> {isOverdue ? "Overdue" : "Open"}
                    </span>
                  </div>
                  <p className="subline">
                    <FaFileAlt /> {course?.title || "Unknown Course"} · {assignment.points} points
                  </p>
                  <p className="desc">{assignment.description}</p>

                  <div className="card-meta">
                    <span>Due: {new Date(assignment.dueDate).toLocaleDateString()}</span>
                    <span>{count} submission(s)</span>
                  </div>

                  <div className="row-actions">
                    {role === "student" && (
                      mySubmission ? (
                        <span className="submitted-pill">
                          <FaCheckCircle /> Submitted
                          {mySubmission.grade !== null && (
                            <>
                              <FaStar /> {mySubmission.grade}
                            </>
                          )}
                        </span>
                      ) : (
                        <button className="btn btn-primary" onClick={() => handleSubmit(assignment)}>
                          Submit
                        </button>
                      )
                    )}

                    {canManageAssignments && (
                      <>
                        <button className="btn btn-outline" onClick={() => openEditAssignment(assignment)}>
                          Edit
                        </button>
                        <button className="btn btn-outline" onClick={() => navigate(`/assignment-submissions/${assignment.id}`)}>
                          View
                        </button>
                      </>
                    )}
                  </div>
                </article>
              );
            })
          )}
        </section>

        {canManageAssignments && (
          <section className="panel">
            <h3>Submissions to Grade</h3>
            <div className="submission-list">
              {submissions.length === 0 ? (
                <p className="muted">No submissions yet.</p>
              ) : (
                submissions.map((submission) => {
                  const assignment = assignments.find((a) => a.id === submission.assignmentId);
                  return (
                    <div className="submission-row" key={submission.id}>
                      <div>
                        <strong>{submission.studentName}</strong>
                        <p className="muted">{assignment?.title || "Assignment"}</p>
                      </div>
                      <div className="submission-controls">
                        {submission.status === "graded" ? (
                          <span className="submitted-pill">
                            <FaCheckCircle /> Graded: {submission.grade}
                          </span>
                        ) : gradingSubmissionId === submission.id ? (
                          <div className="grade-form-inline">
                            <input
                              className="input"
                              type="number"
                              min="0"
                              max="100"
                              placeholder="Grade"
                              value={gradingForm.grade}
                              onChange={(e) =>
                                setGradingForm((prev) => ({ ...prev, grade: e.target.value }))
                              }
                            />
                            <input
                              className="input"
                              placeholder="Feedback"
                              value={gradingForm.feedback}
                              onChange={(e) =>
                                setGradingForm((prev) => ({ ...prev, feedback: e.target.value }))
                              }
                            />
                            <button className="btn btn-primary" onClick={handleGrade}>Save</button>
                          </div>
                        ) : (
                          <button className="btn btn-outline" onClick={() => openGrade(submission.id)}>
                            Grade
                          </button>
                        )}
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </section>
        )}
      </div>
    </Layout>
  );
};

export default Assignments;
