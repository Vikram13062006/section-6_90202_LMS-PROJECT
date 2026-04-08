# Spring Boot Backend Deployment (Public URL)

Use this to deploy the LMS backend and get a public URL for Netlify.

## 1) Local run (for development)

```powershell
Set-Location C:/Projects/my-lms/backend
mvn spring-boot:run
```

Backend runs on `http://localhost:8081` by default.

## 2) Deploy Spring Boot backend on Render (recommended)

1. Push this repository to GitHub.
2. Open Render dashboard -> New + -> Web Service.
3. Connect your GitHub repo.
4. Configure service:
   - Root Directory: `backend`
   - Build Command: `mvn clean package -DskipTests`
   - Start Command: `java -jar target/backend-0.0.1-SNAPSHOT.jar`
5. Add environment variables in Render:
   - `APP_PROFILE=mysql`
   - `MYSQL_URL=jdbc:mysql://<host>:3306/lms_db`
   - `MYSQL_USER=<username>`
   - `MYSQL_PASSWORD=<password>`
   - `JWT_SECRET=ChangeMeChangeMeChangeMeChangeMeChangeMe`
   - `APP_CORS_ALLOWED_ORIGIN_PATTERNS=https://kluniversityeducationlms.netlify.app,https://*.netlify.app`
6. Deploy.

After deploy, Render gives URL like:

`https://your-backend-name.onrender.com`

Your API base URL becomes:

`https://your-backend-name.onrender.com/api`

## 3) Connect Netlify frontend to backend

In Netlify -> Site settings -> Environment variables:

- `VITE_API_URL=https://your-backend-name.onrender.com/api`

Then redeploy Netlify.

## 4) Verify endpoints

Open in browser:

- `https://your-backend-name.onrender.com/api/auth/login`

Expected: not a DNS error (you may see 405/400 depending HTTP method).

## 5) Common issue fix

If registration says backend route not configured:

1. Ensure `VITE_API_URL` is set in Netlify (exact key name).
2. Ensure backend service is healthy and public.
3. Redeploy Netlify after env var change.
