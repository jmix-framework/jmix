## Jmee Project

It is an aggregate subproject of a composite project. It combines the front-end and back-end into one workspace to create a BPM project.

### Backend

Demo project on Jmix to demonstrate the operation of the BPM platform.

To run frontend

```java
gradle :${project_name}-backend:bootRun
```

Documentation: https://docs.jmix.io/jmix/intro.html

### Frontend

To run frontend:
1. Go to directory `${project_name}-frontend`.

   For Windows:
   ```shell
   cd ${project_name}-frontend 
   ```
2. Install dependencies (if not installed yet)
   ```shell
   npm install
   ```
3. Configure parameters for connection to Tasklist backend and Keycloak in `.env.development` file
4. Run dev server:
   ```shell
   npm run dev
   ```
5. Open http://127.0.0.1:3000/ in browser and login with user configured in Keycloak.