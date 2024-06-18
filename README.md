# Currency Conversion API

This application is a Spring Boot based RESTful API for currency conversions.

## Available functions

- `GET /convert`: HTTP GET endpoint that converts the given value from one currency to another.
  - Query parameters: `source`, `target`, `value`
    - `source`: The currency code to convert from as a string
    - `target`: The currency code to convert to as a string
    - `value`: The amount to convert as a double
  - Returns: The converted value as a localized currency string. Locale is based on the 'Accept-Language' header in the request or the default runtime locale if header is not present.

  > **Note:** Input currency codes must comply with the [ISO 4217](https://en.wikipedia.org/wiki/ISO_4217) currency code format.

## Running the application with Docker

> **Note:** As a prerequisite, make sure that Docker is properly installed and running.

To build the application image with Docker, go to the project root directory and run the following command:

```bash
docker build -t currency-api .
```

For running the application, you will require a personal API key for the [SWOP](https://swop.cx) foreign exchange rate API, which is utilized by the application.

When you have the API key, you can then proceed to run the image as a container. To do this, you will pass the API key as an environment variable by replacing the placeholder ${YOUR_SWOP_API_KEY} with your own API key in the following command:

```bash
docker run --name currency-api -it -p 8080:8080 -e "SWOP_API_KEY=${YOUR_SWOP_API_KEY}" currency-api
```

You can then access the application endpoint at <http://localhost:8080/currency-api/>.
