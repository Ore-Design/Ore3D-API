# Ore3D API

![Maven Central Version](https://img.shields.io/maven-central/v/design.ore.api/Ore3D-API?style=for-the-badge&labelColor=%23224946&color=%23FF4C00&link=https%3A%2F%2Fcentral.sonatype.com%2Fartifact%2Fdesign.ore.api%2FOre3D-API)

## Overview

The **Ore3D API** is a public repository designed to facilitate manufacturers and contractors in creating integrations with the proprietary Configure, Price, and Quote (CPQ) software, **Ore3D**. Developed by Ore Designs, Ore3D offers a versatile platform for generating sophisticated quotes, managing manufacturing workflows, and streamlining project collaboration.

With an active Ore3D subscription, users gain the capability to craft their own plugins using this API, extending the functionality of Ore3D according to their specific needs.

## Getting Started

To get started with the Ore3D API, follow these steps:

1. **Add Repository to your Project:**
   JitPack is used to host the public repository. Make sure that you have the repository source listed in your package manager.

   Gradle:
   ```bash
   repositories {
      mavenCentral()
   }
   ```
   Maven already uses the mavenCentral repository, so no need to manually define it.

   Then add the API to your project dependancies.

   Gradle:
   ```bash
   dependencies
   {
      implementation 'com.github.Ore-Design:Ore3D-API:2.6.1'
   }
   ```
   Maven:
   ```bash
   <dependency>
	    <groupId>com.github.Ore-Design</groupId>
	    <artifactId>Ore3D-API</artifactId>
	    <version>2.6.1</version>
	</dependency>
   ```

3. **API Documentation:**
   Explore the detailed documentation in the `docs` directory to understand how to leverage the API effectively.

4. **Subscription and Access:**
   Ensure you have an active Ore3D subscription to access and utilize the features provided by the API.

## Features

- **Plugin Development:**
  Build custom plugins for Ore3D, tailored to your specific manufacturing and quoting requirements.

- **Integration Support:**
  Seamlessly integrate Ore3D functionalities into your existing workflows, enhancing collaboration and efficiency.

- **Subscription-Based Access:**
  Exclusive access to API features is granted to Ore3D subscribers, enabling a secure and scalable integration experience.

## Documentation

Refer to the [official documentation](docs/) for comprehensive details on API usage, endpoint references, and plugin development guidelines.

## Sample Code

Explore the `examples` directory for sample code snippets and reference implementations to jumpstart your Ore3D plugin development.

## Issues

If you encounter any issues or have suggestions for improvement, please [open an issue](https://github.com/Ore-Design/Ore3D-API/issues). Your feedback is highly valued.

## License

This repository and its contents' rights are reserved by Ore Designs Inc. Redistribution of this API serves little to no purpose as it has no functionality without the Ore3D desktop application. Usage of the API is available to all, but once again it serves no purpose without an Ore3D subscription.

## Support

For support and inquiries related to Ore3D or the Ore3D API, contact our support team at [helpdesk@ore.design](mailto:helpdesk@ore.design).

© [Ore Designs Inc.](https://ore.design)
