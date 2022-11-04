MORP comes with actuator endpoints to help diagnosing routing and request authentication issues. The Spring actuator
endpoints are exposed on port 8081 by default. You may find the following endpoints most useful:

* GET `/actuator/gateway/routes` provides a list of routes configured.
* GET `/actuator/gateway/routes/{id}` provides details about a given route based on its ID.
* GET `/actuator/clientregistrations` provides a list of tenants for which client registrations have been created
  dynamically.
* GET `/actuator/clientregistrations/{tenant}` provides details of a client registration for a given tenant.
