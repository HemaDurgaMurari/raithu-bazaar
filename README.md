# Raithu Bazaar

A full-stack, local-first grocery marketplace for fresh vegetables, fruit, snacks, and dairy. Built with Spring Boot 4.1.1 / Java 25, PostgreSQL, React + TypeScript, JWT, Razorpay test checkout, async email jobs, and live STOMP updates.

## What is included

- Public product browsing, search, category filtering, and persistent shopping basket
- Separate customer and seller account registration / sign-in
- Seller product listing, editing, removal, and stock management
- Customer checkout with server-calculated pricing and stock reservation
- Razorpay order creation and server-side HMAC signature verification
- Order confirmation email plus invoice / notification background jobs
- Seller job monitor with live WebSocket updates
- Eighteen sample groceries inserted into an empty database at first startup
- A detailed local setup, payment, and email guide in [HOW_TO_RUN_AND_CONFIGURE.md](./HOW_TO_RUN_AND_CONFIGURE.md)

## Quick start (Windows PowerShell)

1. Install Java 25, Node.js 24, and PostgreSQL. Create a PostgreSQL database named `raithu_bazaar`.
2. In a terminal, open `ecommerce-backend` and set database credentials (change the password to yours):

   ```powershell
   $env:DATABASE_USERNAME = "postgres"
   $env:DATABASE_PASSWORD = "your-local-postgres-password"
   $env:RAZORPAY_KEY_ID = "rzp_test_your_key_id"
   $env:RAZORPAY_KEY_SECRET = "your_test_key_secret"
   .\mvnw.cmd spring-boot:run
   ```

   Razorpay keys can also be stored in a local backend file. From
   `ecommerce-backend`, copy `application-local.properties.example` to
   `application-local.properties`, add the keys there, and start the backend
   normally with `.\mvnw.cmd spring-boot:run`. The local file is ignored by Git.

   **Where to configure the keys:** get the values from the [Razorpay Dashboard](https://dashboard.razorpay.com/) in **Test Mode → Account & Settings → API Keys → Generate Test Key**, then replace the example strings in the two `$env:` commands above with your Test Key ID and Key Secret. Run those commands in the PowerShell terminal opened in `ecommerce-backend`, before starting Spring Boot.

   The backend reads these variables through `ecommerce-backend/src/main/resources/application.properties`, where the existing lines `app.payment.razorpay-key-id=${RAZORPAY_KEY_ID:}` and `app.payment.razorpay-key-secret=${RAZORPAY_KEY_SECRET:}` connect them to Razorpay. **Do not paste your actual credentials into `application.properties`**—keep the placeholders unchanged and supply the values using the terminal commands. Never put the Key Secret in `ecommerce-frontend/.env`, frontend source code, or any `VITE_` variable. If the backend is already running, stop it with Ctrl+C and restart it from the terminal where you set the keys. Without both keys, browsing works but checkout cannot start.

   The wrapper downloads Maven the first time. The API runs at `http://localhost:8080`. If the backend is already running, stop it with Ctrl+C, set these variables in that same terminal, then restart it.
3. In a second terminal, run:

   ```powershell
   cd .\ecommerce-frontend
   Copy-Item .env.example .env
   npm.cmd install
   npm.cmd run dev
   ```

4. Open `http://localhost:5173`. Sign up as a seller to manage the sample products, or as a customer to shop.
5. Before checkout, follow the [Razorpay Test Mode setup and success/failure guide](./HOW_TO_RUN_AND_CONFIGURE.md#connect-razorpay-test-mode-step-by-step) to test successful and failed payments. Razorpay and SMTP are optional until you use payment/email.

## Deployment

The repository includes a Docker Compose setup for the frontend, Spring backend, and PostgreSQL. Follow [Docker Compose deployment](./HOW_TO_RUN_AND_CONFIGURE.md#deploy-the-full-stack-with-docker-compose) and configure the hosted AI key only in the backend environment. Public deployment requires a Docker-capable host and HTTPS configuration.

PowerShell uses `npm.cmd` here because Windows execution policy can block `npm.ps1`.

## Important

- Seller registration is intentionally public per the requested workflow. Any visitor can create a seller account; that account can add/edit/remove catalog items and view order-task statuses. Restrict seller signup before exposing this app outside your trusted local environment.
- Payment cannot complete until `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET` are configured for the backend as described above. Keep the Key Secret out of the frontend `.env`.
- Email runs after server-verified payment. Without SMTP configuration, the email job reports `FAILED` while invoice and notification demo jobs still run.
- This is a local learning/MVP project; review security, inventory operations, refund flows, and privacy requirements before production use.
