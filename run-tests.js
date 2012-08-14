try {
  phantom.injectJs('stacktrace.js');
  phantom.injectJs('target/basil-test.js');
  console.log('Injected target/basil-test.js');
  //basil.core_test.test_ns_hook();
  basil.run_tests.run();
} catch (e) {
  console.log('Found exception');
  console.log(e);
  console.log(e.fileName);
  console.log(e.trace);
  var trace = printStackTrace();
  var lines = trace.join('\n\n');
  console.log(lines);
} finally {
  phantom.exit();
}