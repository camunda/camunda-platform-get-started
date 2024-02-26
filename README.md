# Get started with Camunda 8

This repository gets user started with [Camunda 8](https://camunda.com/platform/) and contains instructions on how to:

- Model your first process
- Create a user task form
- Automate a service task

During this guide, you will use Console, Modeler, Zeebe, Operate, and Tasklist.

# Repository structure

The repository contains the following folders:

- [_csharp_](csharp/) - C# example for deploy, create instance, and a job worker.
- [_go_](go/) - Go example for deploy, create instance, and a job worker.
- [_images_](images/) - Contains screenshots and images for the guide.
- [_java_](java/) - Plain Java example for deploy, create instance, and a job worker.
- [_nodejs_](nodejs/) - Node.js example for deploy, create instance, and a job worker.
- [_process_](process/) - Contains the BPMN process and the user task form.
- [_spring_](spring/) - Spring Boot example for deploy, create instance, and a job worker.

# Configure your environment

To follow this guide, we need a running Camunda 8 cluster. For this purpose,
we use the Camunda 8 SaaS offering at [https://camunda.io](https://camunda.io). If you prefer a local developer setup where you can run the getting started guide against a locally-hosted instance, follow the [Camunda 8 deployment guide](https://docs.camunda.io/docs/self-managed/platform-deployment/overview/).

After the sign-up and log-in at https://camunda.io, [create a new cluster](https://docs.camunda.io/docs/guides/create-cluster/) with the latest stable version of Zeebe and the name **Test Cluster**.

![Camunda Platform Cluster](images/cluster.png)

After the cluster is available, we need to create [client
credentials](https://docs.camunda.io/docs/components/console/manage-clusters/manage-api-clients/).
To do this, take the following steps:

1. From Console, navigate to the **Clusters** detail page.
2. Click on your cluster and switch to the **API** tab.
3. Click **Create new client** and name your client accordingly.

**NOTE: Ensure you keep the generated client credentials in a safe place. The client secret will not be shown again. For your convenience, you can also download the client information to your computer.**

# Model the process

In this example, we'll model a process to send an email message.

Camunda 8 allows you to use the
[BPMN](https://docs.camunda.io/docs/components/modeler/bpmn/bpmn-primer/)
standard to model your business processes. The process consists of two
tasks: a user task to allow a human to enter the message content, and a service
task to automatically send the email message.

![Process](images/send-email.png)

**NOTE: To keep the scope of this guide limited, we only build the scaffolding for this process automation example and will not actually send an email. If you would like to send an email, you may extend the example and connect to a mail provider.**

To create the process, we use [Modeler](https://docs.camunda.io/docs/components/modeler/about-modeler/).

Find the finished process at [process/send-email.bpmn](process/send-email.bpmn).

## Model the user task form

To help users complete the user task **Enter Message**, we create a user
task form using Modeler.

Find the finished form at [process/enter-email-message.form](process/enter-email-message.form).

## Configure the user task to use the form

Now, configure the user task to use the form.
To do this, copy the JSON content of the form into the **Form JSON configuration** field under the **Form** tab in the Modeler properties panel.

![Configure user task form](images/user-task-form.png)

## Configure a service task

The last step is defining the job type of the service task.

The job type is
needed for a job worker to subscribe to the jobs of the task and
complete them. See the [Camunda 8 documentation](https://docs.camunda.io) for more information about
[job workers](https://docs.camunda.io/docs/components/concepts/job-workers/).

![Configure service task](images/configure-service-task.png)

# Deploy process

Deploy the process using [Web
Modeler](#deploy-using-web-modeler), [Desktop Modeler](#deploy-using-desktop-modeler), [zbctl](#deploy-using-zbctl), or your [own code](#deploy-using-code).

## Deploy using Web Modeler

To deploy the [process](process/send-email.bpmn) using Web Modeler,
follow [these steps](https://docs.camunda.io/docs/components/modeler/web-modeler/save-and-deploy/).

## Deploy using Desktop Modeler

To deploy the [process](process/send-email.bpmn) using Desktop Modeler,
follow [these steps](https://docs.camunda.io/docs/components/modeler/desktop-modeler/connect-to-camunda-cloud/).

## Deploy using zbctl

To the deploy the [process](process/send-email.bpmn) using `zbctl`, use the
following command:

```bash
zbctl deploy resource send-email.bpmn \
  --address 365eed98-16c1-4096-bb57-eb8828ed131e.zeebe.camunda.io:443 \
  --clientId 'GZVO3ALYy~qCcD3MYq~sf0GIszNzLE_z' \
  --clientSecret '.RPbZc6q0d6uzRbB4LW.B8lCpsxbBEpmBX0AHQGzINf3.KK9RkzZW1aDaZ-7WYNJ'
```

For **Camunda 8**, we need the cluster id and the [client credentials](#setup-the-environment).

## Deploy using code

Writing your own code allows you to deploy the process (for example, on application
startup) to ensure the latest version is deployed. To see examples of
this, visit the [programming language folders](#repository-structure).

# Start process instance

Start a new instance of the process using [Web Modeler](#start-instance-using-web-modeler), [Desktop Modeler](#start-instance-using-desktop-modeler), [zbctl](#start-instance-using-zbctl), or your [own code](#start-instance-using-code).

## Start instance using Web Modeler

To start an instance of the process using Web Modeler, follow [these steps](https://docs.camunda.io/docs/components/modeler/web-modeler/start-instance/).

## Start instance using Desktop Modeler

After the process is deployed, click **Start Current Diagram** to start an instance of the process with Camunda Modeler.

## Start instance using zbctl

To start a process instance of the BPMN process id `send-email` using `zbctl`,
use the following command:

```bash
zbctl create instance send-email \
  --address 365eed98-16c1-4096-bb57-eb8828ed131e.zeebe.camunda.io:443 \
  --clientId 'GZVO3ALYy~qCcD3MYq~sf0GIszNzLE_z' \
  --clientSecret '.RPbZc6q0d6uzRbB4LW.B8lCpsxbBEpmBX0AHQGzINf3.KK9RkzZW1aDaZ-7WYNJ'
```

For **Camunda 8**, we need the cluster id and the [client credentials](#setup-the-environment).

## Start instance using code

Writing your own code allows you to start an instance of the process on an
external trigger, like an incoming message or a user request. To see examples of this,
visit the [programming language folders](#repository-structure).

# Complete the user task

The first task of the process is the [user task](https://docs.camunda.io/docs/components/modeler/bpmn/user-tasks/)
**Enter Message**.

![Process](images/send-email.png)

To complete the user task, we use Tasklist. To do this, visit the cluster's
details page in Camunda 8 and launch Tasklist.

Take the following steps:

1. In Tasklist, select the **Enter Message** task from the list of tasks.
2. Click the **Claim** button to assign the task to you to work on it.
3. Fill out the **E-Mail Content** field with the message you want to send.
4. Select **Complete Task**.

![Tasklist](images/tasklist.png)

# Complete the service task

The second task of the process is the [service
task](https://docs.camunda.io/docs/components/modeler/bpmn/service-tasks/)
**Send Email**.

![Process](images/send-email.png)

To complete the service task, we need to implement the business logic. Therefore, we must create a [job
worker](https://docs.camunda.io/docs/components/concepts/job-workers/) for
the task type we defined in the diagram. The job worker will subscribe to all
jobs with the same task type.

To define the task type, we select the service task in the diagram and use
the properties panel to set the task type to `email`.

To implement the logic, we implement a job worker. Check out the
programming language [specific folders](#repository-structure) to find the job
worker implementations.

# Further references

Find more information in the [Camunda Platform 8 Documentation](https://docs.camunda.io), or join the [Camunda Platform 8
Forum](https://forum.camunda.io) and [Camunda Platform 8
Slack](https://camunda-cloud.slack.com) community.
