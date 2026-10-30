# Raithu Bazaar — run and connect everything locally

This guide is for the project in this folder. No cloud account or deployment is required. The application uses localhost services and Razorpay **Test Mode**.

## 1. Install the local tools

Install:

- Java JDK 25
- Node.js 24 (npm is included)
- PostgreSQL 16 or newer and pgAdmin (or another PostgreSQL client)

Check in PowerShell:

```powershell
java -version
node --version
npm.cmd --version
```

The repository includes `ecommerce-backend\mvnw.cmd`; it downloads Maven 3.9.11 into your user profile the first time it runs. An internet connection is required for that first run and for downloading project dependencies.

## 2. Create the local database

1. Start the PostgreSQL service.
2. In pgAdmin, connect to your local server.
3. Right-click **Databases → Create → Database**.
4. Name it `raithu_bazaar`, then save.
5. Note the PostgreSQL username and password. The examples below assume user `postgres`; use your own values.

The backend uses Hibernate `ddl-auto=update`, so it creates the tables automatically when it starts. On the first run it adds sample produce items. Do not use this schema setting as a production database migration strategy.

## 3. Configure backend credentials and secrets

Open a PowerShell terminal in `ecommerce-backend`. Set variables in that same terminal before starting Spring Boot. Replace the database password and the JWT value. The JWT secret must be at least 32 characters.

```powershell
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/raithu_bazaar"
$env:DATABASE_USERNAME = "postgres"
$env:DATABASE_PASSWORD = "your-local-postgres-password"
$env:JWT_SECRET = "replace-with-a-private-random-secret-of-32-chars-minimum"
$env:FRONTEND_ORIGIN = "http://localhost:5173"
.\mvnw.cmd spring-boot:run
```

Keep this terminal running. Expected startup is `Started RaithuBazaarApplication`; the API is at `http://localhost:8080`. The project includes safe local defaults, but set your actual database password as shown. Environment variables set in one terminal do not carry over to another.

### Deploy the full stack with Docker Compose

The repository includes a Compose setup for the React site, Spring backend, and persistent PostgreSQL database. It can run on a VPS or a deployment host that supports Docker Compose. The host itself may have a fee; the free model allowance does not pay for hosting.

1. Install Docker Engine with the Compose plugin on the host.
2. Copy `.env.example` to `.env`. Replace `DATABASE_PASSWORD` and `JWT_SECRET` with long random values.
3. Set `FRONTEND_ORIGIN` and `FRONTEND_URL` to the public site origin, including `https://` once TLS is configured.
4. From the project root, run:

   ```sh
   docker compose up --build -d
   ```

5. The frontend listens on port 3000 by default. Put HTTPS/TLS in front of it before sharing the site publicly. PostgreSQL data is stored in the `postgres_data` volume.

The Compose file is a portable starting point. A cloud host may require its own database, networking, health checks, or secret settings; use the same environment variable names in that host's backend service configuration. Checkout also needs Razorpay Test or Live credentials, separately from the AI setup.

### Connect Razorpay Test Mode (step by step)

1. Sign in to the [Razorpay Dashboard](https://dashboard.razorpay.com/) and switch the dashboard to **Test Mode**. Test Mode is separate from Live Mode and does not collect real money.
2. Open **Account & Settings → API Keys** (the dashboard labels may change), choose **Generate Test Key**, and copy both the Key ID (usually starts with `rzp_test_`) and Key Secret. If Razorpay only shows the secret once, save it privately now.
3. In the PowerShell terminal running the backend, press **Ctrl+C** to stop Spring Boot. In that same terminal, set the test credentials and start the backend again:

   ```powershell
   $env:RAZORPAY_KEY_ID = "rzp_test_replace_with_your_key_id"
   $env:RAZORPAY_KEY_SECRET = "replace_with_your_test_key_secret"
   .\mvnw.cmd spring-boot:run
   ```

   Keep this terminal open. These environment variables belong to the backend process; setting them in a different terminal or adding them to the frontend will not connect Razorpay.
4. Leave the frontend running in its own terminal at `http://localhost:5173`. Sign in or create a **customer** account, add products to the basket, choose **Continue to checkout**, and enter a valid 10-digit phone number and delivery address.
5. Choose **Pay securely with Razorpay**. Razorpay Checkout should open with the amount and order ID created by this backend. Complete the popup using a test payment method from Razorpay's [test card details](https://razorpay.com/docs/payments/payments/test-card-details/); do not use real card or bank details. Razorpay's mock bank page has explicit **Success** and **Failure** options so you can exercise either outcome.
6. For a successful test payment, Razorpay returns a signed result to the app. The backend verifies the signature; only then does it mark the order and payment `PAID`, empty the basket, show the order-confirmation banner with its order number, and queue the email/invoice/notification jobs.

**Keep credentials private:** the Key ID is sent to the browser for Razorpay Checkout; the Key Secret is used only by the backend to verify payment signatures. Never put the Key Secret in `ecommerce-frontend\.env`, a `VITE_` variable, source code, screenshots, or chat. The project `.gitignore` excludes local `.env` files, but these two variables can be kept only in the backend terminal as above.

**Switching to Live Mode:** complete Razorpay's account activation/KYC requirements first. Generate a separate **Live Mode** key pair, then configure those values as `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET` in the deployed backend's private environment settings and restart/redeploy it. Do not reuse test keys in production, and do not put the Live Key Secret in Vercel/frontend settings. Verify with a low-value real payment only when ready to accept real charges.

#### What you should see when payment succeeds or fails

- **Order created successfully:** the backend recalculates the amount from its product records, creates a `PENDING` order, and reserves the requested stock. The browser total is not trusted for charging.
- **Payment succeeds:** the signed payment is verified by the backend before the order is shown as confirmed. The basket is cleared and an order-number confirmation appears.
- **Razorpay reports a failed payment:** checkout shows a failure message and the order number. The order is **not confirmed**; it remains pending so you can retry payment within 30 minutes. If your bank/payment app shows a debit even though the site reports failure, do not pay again until you check the payment with Razorpay/the store.
- **You close the Razorpay popup:** the site says payment was not confirmed and the order is still pending. You can retry from the same checkout page; pending orders expire after 30 minutes and the reserved stock is then released.
- **Order validation or payment setup fails before Razorpay opens:** the site shows the error and keeps the basket. Check stock and delivery details, or configure the test credentials and restart the backend.
- **Payment verification is interrupted or uncertain:** do not assume the order succeeded and do not immediately submit another order if a debit appears. Check Razorpay Dashboard → **Transactions/Payments** in Test Mode and contact the store with the displayed order number. The optional webhook below can reconcile a captured payment when the browser callback is unavailable.

Without the two Razorpay environment variables, browsing and account creation still work, but checkout reports that Razorpay is not configured. To inspect local records in pgAdmin, open the `raithu_bazaar` database's Query Tool and run:

```sql
SELECT "order_id", status, total_amount, "created_at"
FROM orders
ORDER BY "created_at" DESC
LIMIT 10;

SELECT payment_id, order_id, status, razorpay_order_id, razorpay_payment_id
FROM payments
ORDER BY payment_id DESC
LIMIT 10;
```

The database is local to this development setup. Never expose customer delivery addresses or payment identifiers when sharing screenshots or logs.

### Optional Razorpay webhook for local development

A Razorpay webhook cannot call `localhost` directly. If you set up a secure public tunnel to port 8080, configure its URL as `<tunnel-url>/api/payment/webhook`, choose the `payment.captured` event, and set the matching webhook secret:

```powershell
$env:RAZORPAY_WEBHOOK_SECRET = "the-webhook-secret-you-entered-in-Razorpay"
```

Restart the backend after changing environment variables. Do not expose a development server publicly unless you understand the tunnel's security implications. The checkout callback already verifies payment without a webhook.

## 4. Configure confirmation email (optional)

For Gmail, enable 2-Step Verification and generate a **Google App Password**. Do not use your normal Gmail password.

Stop the backend, then set these in the backend terminal:

```powershell
$env:MAIL_USERNAME = "your-sending-address@gmail.com"
$env:MAIL_PASSWORD = "your-16-character-google-app-password"
$env:MAIL_FROM = "your-sending-address@gmail.com"
.\mvnw.cmd spring-boot:run
```

After a verified payment, the EMAIL background job sends the order confirmation to the signed-in customer's email. Invoice and notification jobs are local demo tasks. If SMTP is not configured or the provider rejects the login, the email task is marked `FAILED` and the reason is shown in the seller's activity monitor.

## 5. Run the website

Open a **second** PowerShell terminal in the project folder:

```powershell
cd .\ecommerce-frontend
Copy-Item .env.example .env
npm.cmd install
npm.cmd run dev
```

Open the local address Vite prints (normally `http://localhost:5173`). Keep both the backend and Vite terminals open.

The frontend `.env` contains only:

```dotenv
VITE_API_URL=http://localhost:8080
```

Change that URL only if your backend uses another local port. Never put a database password, JWT secret, Razorpay Key Secret, or email password into frontend `.env`; `VITE_` variables are embedded into the public browser bundle.

## 6. Create the two account types

- Select **Join us → Shop fresh** to register as a customer. Customers can build a basket and pay.
- Select **Join us → Sell local** to register as a seller. Sellers can add produce, update stock/details, remove catalog items, and view background-task progress.
- Sign-in recognizes the account's server-stored role and opens the correct area.

Seller signup is publicly enabled because that is the requested behavior. For any shared/public deployment, change it to invitation or administrator approval before allowing untrusted users to sign up as sellers.

## 7. Try the buying flow

1. Sign up/sign in as a customer.
2. Add a few items to the basket and select **Continue to checkout**.
3. Enter a name, 10-digit phone number, and delivery address.
4. Follow **Connect Razorpay Test Mode (step by step)** above, then complete the Razorpay test checkout.
5. On success, verify the order-number confirmation appears and the basket clears. On a test failure or popup close, verify that the checkout explains the order is not confirmed and how to retry safely.
6. The backend verifies the Razorpay HMAC signature before marking the order paid and starting the email, invoice, and notification jobs.
7. Sign into a seller account and check **Seller studio → Order activity** for background-job status changes. That screen monitors post-payment jobs; use the pgAdmin queries above to inspect local order/payment statuses.

The backend calculates order totals from product records and reserves the available stock when an order is created. Unpaid pending orders expire after 30 minutes and their stock is released.

## 8. Build and troubleshoot

Backend package/build check:

```powershell
cd .\ecommerce-backend
.\mvnw.cmd verify
```

Frontend production build check:

```powershell
cd .\ecommerce-frontend
npm.cmd run build
```

Common fixes:

- **Database connection refused:** Start PostgreSQL and confirm database name, port, username, and password.
- **Port 8080 already in use:** Close the other backend or set `$env:PORT = "8081"` and change frontend `VITE_API_URL` to `http://localhost:8081`.
- **PowerShell blocks npm:** Use `npm.cmd`, not `npm`.
- **Browser says CORS/network error:** Keep Vite on port 5173 and set backend `FRONTEND_ORIGIN` to exactly `http://localhost:5173` (or `http://127.0.0.1:5173` if using that address); restart the backend after changes.
- **Razorpay configuration error:** Set the test Key ID and Key Secret in the backend terminal, then restart Spring Boot.
- **Email job fails:** Check Gmail App Password, SMTP access, `MAIL_USERNAME`, `MAIL_PASSWORD`, and `MAIL_FROM`. The failed task is still visible in Seller studio.
- **Missing images:** Product photos use remote Unsplash URLs and need an internet connection; seller product photo URLs can be edited in Seller studio.

## Local security reminders

- Never commit real secrets or share them in screenshots/chats.
- Rotate any secret that is exposed.
- The seller signup endpoint is public and grants catalog-management access by design for this local project.
- Razorpay signature verification and role checks happen on the backend; frontend-only checks are not security boundaries.
