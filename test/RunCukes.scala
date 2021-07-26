import io.cucumber.junit.{Cucumber, CucumberOptions}
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("features"),
  glue = Array("steps"),
  tags = Array("not @pending"),
  strict = true,
  plugin = Array("pretty", "html:target/reports/cucumber")
)
class RunCukes
