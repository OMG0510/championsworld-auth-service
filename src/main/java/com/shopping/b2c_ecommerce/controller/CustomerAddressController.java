package com.shopping.b2c_ecommerce.controller;

import com.shopping.b2c_ecommerce.dto.AddAddressRequest;
import com.shopping.b2c_ecommerce.dto.AddressResponse;
import com.shopping.b2c_ecommerce.dto.UserAddressResponse;
import com.shopping.b2c_ecommerce.dto.UserIdentity;
import com.shopping.b2c_ecommerce.entity.Address;
import com.shopping.b2c_ecommerce.service.AddressService;
import com.shopping.b2c_ecommerce.util.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer/addresses")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerAddressController {

    private static final Logger log = LoggerFactory.getLogger(CustomerAddressController.class);

    private final AddressService addressService;
    private final Utilities utilities;

    public CustomerAddressController(AddressService addressService, Utilities utilities) {
        this.addressService = addressService;
        this.utilities = utilities;
    }

    // ADD ADDRESS (Customer)
    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(
            @RequestBody AddAddressRequest request
    ) {
        log.info("Add address request received");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UserIdentity identity)) {
            log.warn("Add address failed. Unauthorized access");
            return ResponseEntity.status(401)
                    .body(new AddressResponse(null, false, "Unauthorized"));
        }

        log.info("Adding address for userId={}", identity.getUserId());

        Address address = addressService.addAddress(identity.getUserId(), request);

        log.info("Address added successfully. addressId={}, userId={}",
                address.getId(),
                identity.getUserId()
        );

        return ResponseEntity.ok(
                new AddressResponse(
                        address.getId(),
                        address.getIsDefault(),
                        "Address added successfully"
                )
        );
    }

    //GET ADDRESS (CUSTOMER)
    @GetMapping
    public ResponseEntity<List<UserAddressResponse>> getUserAddresses() {

        log.info("Fetch addresses request received");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UserIdentity identity)) {
            log.warn("Fetch addresses failed. Unauthorized access");
            return ResponseEntity.status(401).build();
        }

        Long userId = identity.getUserId();
        log.info("Fetching addresses for userId={}", userId);

        List<Address> addresses = addressService.getAddressesByUserId(userId);

        List<UserAddressResponse> response = addresses.stream()
                .map(utilities::mapToResponse)
                .toList();

        log.info("Addresses fetched successfully. count={}, userId={}",
                addresses.size(), userId);

        return ResponseEntity.ok(response);
    }

    // UPDATE ADDRESS (Customer)
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable Long addressId,
            @RequestBody AddAddressRequest request
    ) {
        log.info("Update address request received. addressId={}", addressId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UserIdentity identity)) {
            return ResponseEntity.status(401)
                    .body(new AddressResponse(null, false, "Unauthorized"));
        }

        Address updatedAddress =
                addressService.updateAddress(identity.getUserId(), addressId, request);

        return ResponseEntity.ok(
                new AddressResponse(
                        updatedAddress.getId(),
                        updatedAddress.getIsDefault(),
                        "Address updated successfully"
                )
        );
    }

    // DELETE ADDRESS (Customer)
    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(
            @PathVariable Long addressId
    ) {
        log.info("Delete address request received. addressId={}", addressId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UserIdentity identity)) {
            return ResponseEntity.status(401).build();
        }

        addressService.deleteAddress(identity.getUserId(), addressId);

        return ResponseEntity.ok("Address Deleted Successfully");
    }
}