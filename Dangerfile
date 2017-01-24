# Print reports for each test result
Dir.glob('stag-library/build/test-results/*.xml') do |result|
    junit.parse result
    junit.report
end