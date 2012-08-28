try {
  phantom.injectJs('target/basil-test.js');
  console.log('Injected target/basil-test.js');
  basil.run_tests.run();
} catch (e) {
  console.log('Found exception');
  console.log(e);
  console.log(e.fileName);
  console.log(e.trace);
} finally {
  phantom.exit();
}