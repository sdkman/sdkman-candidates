import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("features"),
  glue = Array("steps"),
  tags = Array("~@pending"),
  strict = true,
  plugin = Array("pretty", "html:target/reports/cucumber")
)
class RunCukes
