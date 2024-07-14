# Currency Conversion API

This application is a Spring Boot based RESTful API that can be used to convert currency values from one currency to another using real-time data from the [SWOP](https://swop.cx) foreign exchange rate API.

## Available functions

- `GET /convert`: HTTP GET endpoint that converts the given value from one currency to another.
  - Query parameters: `source`, `target`, `value`
    - `source`: The currency code to convert from as a string
    - `target`: The currency code to convert to as a string
    - `value`: The amount to convert as a double
  - Returns: The converted value as a localized currency string. Locale is based on the 'Accept-Language' header in the request or the default runtime locale if header is not present.

  > **Note:** Input currency codes must comply with the [ISO 4217](https://en.wikipedia.org/wiki/ISO_4217) currency code format.

## Features

- Uses real-world and real-time conversion rates from the [SWOP](https://swop.cx) foreign exchange rate API
- Supports all currencies and conversion pairs that are available from SWOP API
- Smart caching is used to limit the number and rate of calls made to SWOP API
- Resilience measures are used to handle communication errors with SWOP API gracefully
- Authentication is provided by using an API key
- Security includes CSRF protection and CSP protection
- Observability is provided by sending metrics to [InfluxDB](https://www.influxdata.com/products/influxdb/) and displaying them in a [Grafana](https://grafana.com/) monitoring dashboard
- Unit tests for services and controllers with JUnit and Mockito
- Dockerization of the application and its monitoring dependencies

## Running the application with Docker

> **Note:** As a prerequisite, make sure that Docker is properly installed and running.

For running the application, you will require a personal API key for the [SWOP](https://swop.cx) foreign exchange rate API, which is utilized by the application. You can get free or paid accounts for accessing the API. When you have the API key associated with your account, you can then proceed to run the image with Docker by passing the API key as an environment variable to the Docker container.

To build and run the application and its monitoring dependencies with Docker, go to the project root directory and run the following command:

```bash
docker-compose up -d -e "CURRENCY_API_KEY=${YOUR_CURRENCY_API_KEY}" -e "SWOP_API_KEY=${YOUR_SWOP_API_KEY}" -e "INFLUX_TOKEN=${YOUR_INFLUX_TOKEN}"
```

In the command you must replace the placeholder ${YOUR_SWOP_API_KEY} with your own SWOP API key retrieved from the service. Also, you must define values for the environment variables CURRENCY_API_KEY and INFLUX_TOKEN, which are used by the application for authentication of received requests to the REST API and for securing the connection to the InfluxDB database used by the application, respectively.

Alternatively, you can create a new file named `.env` in the root of the project and define the environment variables inside the file as shown below:

```bash
CURRENCY_API_KEY=${YOUR_CURRENCY_API_KEY}
INFLUX_TOKEN=${YOUR_INFLUX_TOKEN}
SWOP_API_KEY=${YOUR_SWOP_API_KEY}
```

And then run the following command:

```bash
docker-compose up -d
```

## Using the application

After the build and start-up process from the previous section is finished, the application endpoint is available at <http://localhost:8080/currency-api/convert> as defined in the [Available functions](#available-functions) section. Grafana monitoring tool for observing the application is also available at <http://localhost:3000/> with a ready-made dashboard named `Currency API Monitoring Dashboard`. The dashboard is available in the `Dashboards` section in the Grafana UI.

To convert a value from one currency to another (example: 100 EUR to USD), you can use the following HTTP GET request:

```bash
http://localhost:8080/currency-api/convert?source=EUR&target=USD&value=100
```

The response will contain the converted value as a localized currency string (example output: `$107.01`).

All requests to the endpoint must be authenticated with an API key. The required API key is defined in the environment variable CURRENCY_API_KEY. The authentication is provided by including the header `X-API-KEY` in the request with the value `${YOUR_CURRENCY_API_KEY}`.

An example using `curl` is as follows:

```bash
curl -G -d "source=EUR" -d "target=USD" -d "value=100" http://localhost:8080/currency-api/convert -H "X-API-KEY: ${YOUR_CURRENCY_API_KEY}"
```
