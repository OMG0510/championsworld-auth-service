package com.shopping.b2c_ecommerce.service;

import com.shopping.b2c_ecommerce.dto.AddAddressRequest;
import com.shopping.b2c_ecommerce.entity.Address;
import com.shopping.b2c_ecommerce.entity.User;
import com.shopping.b2c_ecommerce.repository.AddressRepository;
import com.shopping.b2c_ecommerce.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shopping.b2c_ecommerce.exception.AddressNotFoundException;
import com.shopping.b2c_ecommerce.exception.UserNotFoundException;
import com.shopping.b2c_ecommerce.exception.UnauthorizedAddressAccessException;

import java.util.List;

@Service
public class AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressService.class);

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    //  Add Address
    @Transactional
    public Address addAddress(Long userId, AddAddressRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        boolean isFirstAddress = !addressRepository.existsByUserId(userId);

        Address address = new Address();
        address.setUser(user);
        address.setLabel(request.getLabel());
        address.setFirstName(request.getFirstName());
        address.setLastName(request.getLastName());
        address.setContactNumber("91" + request.getContactNumber());
        address.setEmail(request.getEmail());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setCountry(request.getCountry());

        boolean makeDefault =
                isFirstAddress || Boolean.TRUE.equals(request.getIsDefault());

        if (makeDefault) {
            addressRepository.clearDefaultForUser(userId);
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }

        return addressRepository.save(address);
    }

    // Get Address
    public List<Address> getAddressesByUserId(Long userId) {

        log.info("Fetch addresses service called. userId={}", userId);

        if (!userRepository.existsById(userId)) {
            log.warn("User not found. userId={}", userId);
            throw new UserNotFoundException(userId);
        }

        return addressRepository.findByUserId(userId);
    }

    //  Update Address
    @Transactional
    public Address updateAddress(Long userId, Long addressId, AddAddressRequest request) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new UnauthorizedAddressAccessException();
        }

        address.setLabel(request.getLabel());
        address.setFirstName(request.getFirstName());
        address.setLastName(request.getLastName());
        address.setContactNumber("91" + request.getContactNumber());
        address.setEmail(request.getEmail());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setCountry(request.getCountry());

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.clearDefaultForUser(userId);
            address.setIsDefault(true);
        }

        return addressRepository.save(address);
    }

    //  Delete Address
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new UnauthorizedAddressAccessException();
        }

        boolean wasDefault = address.getIsDefault();

        addressRepository.delete(address);

        if (wasDefault) {
            addressRepository.findByUserId(userId)
                    .stream()
                    .findFirst()
                    .ifPresent(a -> {
                        a.setIsDefault(true);
                        addressRepository.save(a);
                    });
        }
    }

}