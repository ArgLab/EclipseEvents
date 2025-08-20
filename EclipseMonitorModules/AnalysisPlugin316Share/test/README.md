# Runtime Analysis Plugin - Test Suite

## Overview
This test suite provides comprehensive unit testing for the Runtime Analysis Plugin components.

## Test Structure

### Core Package Tests
- **AnalysisEngineTest**: Tests the main analysis orchestrator including test data generation, timeout handling, and measurement scaling
- **CodeScannerTest**: Tests reflection-based method discovery and class loading
- **ExecutionResultTest**: Tests result data structure and status tracking

### Models Package Tests  
- **AlgorithmRegistryTest**: Tests workshop-based algorithm organization and configuration retrieval
- **AlgorithmConfigTest**: Tests algorithm configuration data structure
- **MethodSignatureTest**: Tests method signature representation and parameter type handling

### Handlers Package Tests
- **RunAnalysisHandlerTest**: Tests Eclipse command handler integration (requires Eclipse framework mocks)

### Dialogs Package Tests
- **AlgorithmSelectionDialogTest**: Tests algorithm selection UI (requires SWT testing setup)

### Views Package Tests
- **RuntimeAnalysisViewTest**: Tests chart display and data visualization (requires SWT/JFreeChart testing)

## Testing Dependencies Required

### Core Testing
- JUnit 5 (jupiter)
- Mockito (for mocking dependencies)

### Eclipse/SWT Testing  
- SWTBot (for UI testing)
- Eclipse Test Framework
- Mock Eclipse workspace and project structures

### Chart Testing
- JFreeChart test utilities
- AWT/Swing testing support

## Key Testing Areas

### Data Accuracy
- Test data generation for different parameter types
- Measurement scaling (0ms -> 1 -> 0.1ms display conversion)
- Median calculation from multiple runs

### Error Handling
- Missing algorithm implementations
- Timeout scenarios
- Reflection failures
- Invalid method signatures

### Integration Points
- Eclipse workspace integration
- SWT-AWT bridge functionality
- Chart data visualization
- UI state management

## Running Tests

1. Ensure all dependencies are on classpath
2. Set up Eclipse test workspace if testing UI components
3. Run individual test classes or full suite
4. Check console output for detailed logging during tests

## Notes for Implementation

- Mock Eclipse framework components for handler/view tests
- Use test doubles for expensive operations (reflection, file I/O)
- Consider parameterized tests for testing multiple workshop configurations
- Test both positive and negative cases for all public methods
- Verify logging output where applicable