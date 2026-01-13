-- name: CreateStudentCV :one
INSERT INTO student_cvs (user_id, title, summary, skills, education, experience, projects, certifications, languages, contact_info)
VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
RETURNING *;

-- name: GetStudentCVByUserID :one
SELECT * FROM student_cvs WHERE user_id = $1;

-- name: UpdateStudentCV :exec
UPDATE student_cvs SET
    title = COALESCE($2, title),
    summary = COALESCE($3, summary),
    skills = COALESCE($4, skills),
    education = COALESCE($5, education),
    experience = COALESCE($6, experience),
    projects = COALESCE($7, projects),
    certifications = COALESCE($8, certifications),
    languages = COALESCE($9, languages),
    contact_info = COALESCE($10, contact_info),
    is_public = COALESCE($11, is_public),
    pdf_url = COALESCE($12, pdf_url),
    updated_at = NOW()
WHERE user_id = $1;

-- name: DeleteStudentCV :exec
DELETE FROM student_cvs WHERE user_id = $1;

-- name: GetPublicCVs :many
SELECT cv.*, u.username, u.full_name, u.avatar_url
FROM student_cvs cv
JOIN users u ON cv.user_id = u.id
WHERE cv.is_public = TRUE
ORDER BY cv.updated_at DESC
LIMIT $1 OFFSET $2;
