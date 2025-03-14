# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.4.0] - 2025-03-11

### Added

- Add version parameter to Main method [#393](https://github.com/nbbrd/beanzooka/issues/393)
- Add deployment to Maven Central [#392](https://github.com/nbbrd/beanzooka/issues/392)

### Fixed

- Fix auto reload of latest workspace if input file is not found [#394](https://github.com/nbbrd/beanzooka/issues/394)

### Changed

- Modernize use of NIO API

## [1.3.0] - 2024-07-16

### Added

- Add relaunch button [#318](https://github.com/nbbrd/beanzooka/issues/318)
- Add autofill actions [#320](https://github.com/nbbrd/beanzooka/issues/320)
- Add auto reload of latest workspace [#321](https://github.com/nbbrd/beanzooka/issues/321)

### Fixed

- Fix version in title bar [#319](https://github.com/nbbrd/beanzooka/issues/319)

## [1.2.0] - 2024-06-04

### Added

- Add distribution to Scoop (Windows)
- Add distribution to Homebrew (macOS & Linux)

### Fixed

- Fix reordering by dnd [#32](https://github.com/nbbrd/beanzooka/issues/32)

### Changed

- Remove specific binary for Windows
- Change look&feel to FlatLightLaf [#138](https://github.com/nbbrd/beanzooka/issues/138)

## [1.1.0] - 2019-09-25

This is a feature release of **Beanzooka**.   
Beanzooka follows [semantic versioning](https://semver.org/).

### Added

- Add autofill in editor
- Add file filter in editor
- Add relaunch command on session panel
- Add 'copy path' to contextual menu
- Add reordering by drag & drop in editor
- Add duplicate action in editor
- Add automatic configuration loading through command line
- Add splashscreen when using bin.jar

### Fixed

- Fix detection of portable Java by using %JAVA_HOME% variable on Windows
- Fix default console logger
- Fix missing icon in editor
- Fix Windows search scope in editor

## [1.0.0] - 2019-03-20

This is the initial release of **Beanzooka**.   
Beanzooka follows [semantic versioning](https://semver.org/).

[Unreleased]: https://github.com/nbbrd/beanzooka/compare/v1.4.0...HEAD
[1.4.0]: https://github.com/nbbrd/beanzooka/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/nbbrd/beanzooka/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/nbbrd/beanzooka/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/nbbrd/beanzooka/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/nbbrd/beanzooka/releases/tag/v1.0.0
