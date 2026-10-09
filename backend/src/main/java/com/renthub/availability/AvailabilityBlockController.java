package com.renthub.availability;

import com.renthub.audit.AuditService;
import com.renthub.auth.AuthService;
import com.renthub.car.CarRepository;
import com.renthub.property.PropertyRepository;
import com.renthub.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@RequestMapping("/api/availability-blocks")
@CrossOrigin(origins="http://localhost:5173")
public class AvailabilityBlockController {
 private final AvailabilityBlockRepository repo; private final AvailabilityPolicyService policy; private final AuthService auth; private final CarRepository cars; private final PropertyRepository props; private final AuditService audit;
 public AvailabilityBlockController(AvailabilityBlockRepository repo,AvailabilityPolicyService policy,AuthService auth,CarRepository cars,PropertyRepository props,AuditService audit){this.repo=repo;this.policy=policy;this.auth=auth;this.cars=cars;this.props=props;this.audit=audit;}
 @GetMapping("/{type}/{assetId}") public List<AvailabilityBlock> list(@RequestHeader(value="Authorization",required=false)String h,@PathVariable String type,@PathVariable Long assetId){User u=auth.require(h);auth.requireRole(u,"OWNER","ADMIN");authorize(u,type,assetId);return repo.findByAssetTypeAndAssetIdOrderByStartDateDesc(type.toUpperCase(),assetId);}
 @PostMapping public AvailabilityBlock create(@RequestHeader(value="Authorization",required=false)String h,@RequestBody AvailabilityBlock block){User u=auth.require(h);auth.requireRole(u,"OWNER","ADMIN");String type=block.getAssetType()==null?"":block.getAssetType().toUpperCase();authorize(u,type,block.getAssetId());AvailabilityBlock saved=policy.create(type,block.getAssetId(),block.getStartDate(),block.getEndDate(),block.getReason(),block.getBlockType(),u.getId());audit.record(u.getId(),"AVAILABILITY_BLOCK_CREATED","AVAILABILITY_BLOCK",saved.getId(),null,type+"#"+block.getAssetId());return saved;}
 @DeleteMapping("/{id}") public void remove(@RequestHeader(value="Authorization",required=false)String h,@PathVariable Long id){User u=auth.require(h);auth.requireRole(u,"OWNER","ADMIN");AvailabilityBlock b=repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Block not found"));authorize(u,b.getAssetType(),b.getAssetId());if("MAINTENANCE".equalsIgnoreCase(b.getBlockType()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Maintenance blocks must be changed from the maintenance workflow");repo.delete(b);audit.record(u.getId(),"AVAILABILITY_BLOCK_REMOVED","AVAILABILITY_BLOCK",id,b.getReason(),null);}
 private void authorize(User u,String rawType,Long assetId){String type=rawType==null?"":rawType.toUpperCase();if("ADMIN".equalsIgnoreCase(u.getRole()))return;if("CAR".equals(type)){var c=cars.findById(assetId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Car not found"));if(!u.getId().equals(c.getOwnerId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You do not own this car");return;}if("PROPERTY".equals(type)){var p=props.findById(assetId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Property not found"));if(!u.getId().equals(p.getOwnerId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You do not own this property");return;}throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported asset type");}
}
