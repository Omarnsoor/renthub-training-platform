package com.renthub.car;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/cars") @CrossOrigin(origins="http://localhost:5173")
public class CarController {
  private final CarRepository repo; public CarController(CarRepository repo){this.repo=repo;}
  @GetMapping public List<Car> all(){return repo.findAll();}
  @GetMapping("/{id}") public Car one(@PathVariable Long id){return repo.findById(id).orElseThrow();}
  @PostMapping public Car create(@RequestBody Car car){return repo.save(car);}
}
