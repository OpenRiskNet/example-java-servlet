# Example Templates

__NOTE__: This is work in progress.

This examples assume you have cloned or forked the openrisknet/example-java-servlet repository and have a local copy.
Alternatively you can run directly from GitHub by using the full URL to the raw version of the file.

In the examples below $PATH\_TO\_REPO would either be the rull or relative path to your local copy or something like:
https://raw.githubusercontent.com/OpenRiskNet/example-java-servlet/master/

For simplicity these examples assume you are working in an empty, disposable project. At various stages we delete
EVEYTHING from the project so that we can start from fresh using this:

```
$ oc delete all --all
```

Beware that thus will do exactly what it says on the can for your current project.

This guide concentrate on using the CLI, though much if not all of this can be done from the web console.
It's good to have the web console open at the overview page of your project so that you can see that action happening
as you do it.

**NOTE**: the image used runs as root so you must change the security settings as described 
[here](https://github.com/OpenRiskNet/home/blob/master/openshift/docker_userids.md).
The simplest (but not secure) approach is to change the runAsUser value to RunAsAny.

## Setting security constraints

The openrisknet/example-java-servlet image follows reasonably good practices as it runs as a non-root user.
If you look at the [build.gradle](../../build.gradle) you will see that a user with UID 501 is created and this user
is added to the root group (GID 0), and that ownership of the tomcat files are changed to that user.
That is good practice (not running as the root user) but its not good enough for the default Openshift security settings
as by default OpenShift runs the container using randomly assigned non privileged user, not our 501 user.
So we need to adjust the default settings. Firstly we need to tell the pod to run as the specified user. In the templates 
you will see a section like this:

```
securityContext:
- runAsUser: MustRunAsNonRoot
```

This tells OpenShift that the pod should be run as the user specified in the Dockerfile, but that user cannot be the root user.
See [here](https://docs.openshift.org/latest/architecture/additional_concepts/authorization.html#security-context-constraints) 
for more details.

Also, we need to give the user who is doung the deployment privileges to do this, as by default normal users belong to the 
`restricted` security context constraint (SCC). One way to do this is to allow this for all authenticated users like this:

```
oc adm policy add-scc-to-group anyuid system:authenticated
```

See [here](https://docs.openshift.org/latest/admin_guide/manage_scc.html#enable-images-to-run-with-user-in-the-dockerfile)
for more details.

## pod.yml

Creates a standalone pod from a Docker image. If the pod fails of is terminated it is not replaced.
This is not really useful, but provides the most basic example of how to use a template to create something
in Openshift, and is used to illustrate some basic patterns.

To view the template:

```
$ cat $PATH_TO_REPO/openshift/templates/pod.yml
```

To process the template and convert it into object definitions:

```
$ oc process -f $PATH_TO_REPO/openshift/templates/pod.yml
```

or, better for readability, in YAML format:

```
$ oc process -o yaml -f $PATH_TO_REPO/openshift/templates/pod.yml
```

This just give the definitions of the pod to create. To actually create it:

```
$ oc process -f $PATH_TO_REPO/openshift/templates/pod.yml | oc create -f -
```

Now check what is present

```
$ oc get all
```

You will see one pod. To see more about it:

```
$ oc describe po/example-java-servlet
```

Creating this pod works, and our Docker image is working. But how do we know this? We can't access the pod and there is no service or route set up. All we can really do is look at the logs, or ssh to the contianer. Let's ssh to it:

```
$ oc rsh po/example-java-servlet
$ curl "http://localhost:8080"
Hello unauthenticated user
```

Finally, our app uses an environment variable to configure the greeting it displays, and the template is aware of this,
so to use this feature try this:

```
$ oc delete all --all
$ oc process -f $PATH_TO_REPO/openshift/templates/pod.yml -p GREETING=WTF | oc create -f -
```

(note that we first need to delete the old pod before deploying it again).
If you check the results again you will see the message `WTF unauthenticated user`.

So far so good, but orphan pods aren't much use. We really need then to be created by a replication controller.

## replication-controller.yml

The main purpose of using a replication controller is:

1. to provide resiliance - if a pod fails it will be replaced with a new one
1. to provide scalability - you can increase and decrease the number of pods that are running

Our replication controller template handles creating the pod that the previous step created.

First clean up from before:

```
$ oc delete all --all
```

Now apply the template and create the objects (we'll just jump to the action here, though you can check it out step 
by step as before if you prefer):

```
$ oc process -f $PATH_TO_REPO/openshift/templates/replication-controller.yml | oc create -f -
```

Let's see the pods:

```
$ oc get pods
NAME                         READY     STATUS    RESTARTS   AGE
example-java-servlet-r9fs4   1/1       Running   0          58m
```

You can also look in the web console and you will see the pod running.

Now let's scale the pod:

```
$ oc scale --replicas=3 replicationcontrollers example-java-servlet
replicationcontroller "example-java-servlet" scaled
```

```
$ oc get pods
NAME                         READY     STATUS    RESTARTS   AGE
example-java-servlet-bx459   1/1       Running   0          7s
example-java-servlet-r9fs4   1/1       Running   0          1h
example-java-servlet-tjkpk   1/1       Running   0          7s
```

Pretty good. But we still can't really access the app properly. To do so we need a service.

## service.yml

A service makes the pods accessible within the Kubernetes cluster, and provides load balancing between replicas of the pod.
The create the service as well as the replication controller use the service.yml template.

```
$ oc delete all --all
$ oc process -f $PATH_TO_REPO/openshift/templates/service.yml | oc create -f -
replicationcontroller "example-java-servlet" created
service "example-java-servlet" created
```

Now you will see a pod, its replication controller and the service

```
$ oc get all
NAME                      DESIRED   CURRENT   READY     AGE
rc/example-java-servlet   1         1         1         1m

NAME                       CLUSTER-IP    EXTERNAL-IP   PORT(S)    AGE
svc/example-java-servlet   172.30.39.6   <none>        8080/TCP   1m

NAME                            READY     STATUS    RESTARTS   AGE
po/example-java-servlet-86p85   1/1       Running   0          1m
```

The service allows acces to the app from within the cluster. But how to access it from outside?
You need to create a route to do this.

## route.yml

A route provides access from outside the cluster, providing a proxy to the service

```
$ oc delete all --all
$ oc process -f $PATH_TO_REPO/openshift/templates/route.yml | oc create -f -
replicationcontroller "example-java-servlet" created
service "example-java-servlet" created
route "example-java-servlet" created
```

Let's see what we now have:

```
$ oc get all
NAME                      DESIRED   CURRENT   READY     AGE
rc/example-java-servlet   1         1         1         5s

NAME                          HOST/PORT                                             PATH      SERVICES               PORT      TERMINATION   WILDCARD
routes/example-java-servlet   example-java-servlet-myproject.192.168.64.18.nip.io             example-java-servlet   http                    None

NAME                       CLUSTER-IP       EXTERNAL-IP   PORT(S)    AGE
svc/example-java-servlet   172.30.212.193   <none>        8080/TCP   5s

NAME                            READY     STATUS    RESTARTS   AGE
po/example-java-servlet-7frj3   1/1       Running   0          5s
```
Notice the route. This tells you that you should be able to access the app at http://example-java-servlet-myproject.192.168.64.18.nip.io

## Conclusion

That's it for this exercise. Note that in all cases we've been using a pre-built Docker image that already exists on Docker Hub.
The next exercise will deal with how to build an image for an app from its source code.



