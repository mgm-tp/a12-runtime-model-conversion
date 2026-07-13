<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://www.mgm-tp.com/global-content/cd/logos/a12/app-icons/dark/A12-Dark.svg" />
  <img src="https://www.mgm-tp.com/global-content/cd/logos/a12/app-icons/light/A12-Light.svg" height="200" alt="A12 logo" />
</picture>

# Runtime Model Conversion (RMC)

The Runtime Model Conversion library converts A12 workspace models into A12 runtime models.
It is intended to be used within the A12 Workspace Conversion Framework (WCF).

Refer to https://geta12.com/#/docs to get started with A12 development

---

## License

Parts of the A12 platform are made available under a **dual license**.
Please check the [LICENSE](./LICENSE) file for details.

---

## Getting Started

### How to Build and Run

#### Prerequisites

| Tool                 |   Version |
| -------------------- | --------: |
| [JDK]                |    `21`   |
| [Gradle]             |   `9.0.0` |

---

#### How to Build

```sh
gradle assemble
```

#### How to Test

```sh
gradle check
```

#### How to Apply Code Formatting

[Spotless] is used to enforce consistent code formatting.

To automatically apply formatting:

```sh
gradle spotlessApply
```

To check for violations without modifying files:

```sh
gradle spotlessCheck
```

---

### How to Add a Converter

A converter is a class that implements `WorkspaceConverter` and is annotated with `@WcfConverter`.

```java
@WcfConverter(
    order = 50,
    description = "...")
public class MyModelConverter implements WorkspaceConverter {

    @Override
    public Workspace convert(Workspace workspace) {
        // custom workspace conversion
        return workspace;
    }
}
```

- **`order`** controls the execution sequence relative to other converters (lower = earlier).
- Access models via `workspace.getModels()` and files via `workspace.getFiles()`.

---

### Documentation

- Full technical documentation is available at [GetA12.com](https://GetA12.com).
- The website also provides access to the **A12 Discourse Community Forum**.

---

**The mgm A12 Team**

[mgm technology partners GmbH](https://www.mgm-tp.com) • [Imprint](https://www.mgm-tp.com/imprint.html)

<!--- References --->

[JDK]: https://www.oracle.com/technetwork/java/javase/overview/index.html
[Gradle]: https://docs.gradle.org/
[Spotless]: https://github.com/diffplug/spotless
