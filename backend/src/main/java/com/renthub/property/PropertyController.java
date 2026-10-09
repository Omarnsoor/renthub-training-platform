package com.renthub.property;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/properties") @CrossOrigin(origins="http://localhost:5173")
public class PropertyController {
  private final PropertyRepository repo; public PropertyController(PropertyRepository repo){this.repo=repo;}
  @GetMapping public List<Property> all(){return repo.findAll();}
  @GetMapping("/{id}") public Property one(@PathVariable Long id){return repo.findById(id).orElseThrow();}
  @PostMapping public Property create(@RequestBody Property p){return repo.save(p);}
}
