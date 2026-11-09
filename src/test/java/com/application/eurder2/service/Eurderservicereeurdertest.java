package com.application.eurder2.service;

import com.application.eurder2.domain.Eurder;
import com.application.eurder2.domain.Item;
import com.application.eurder2.domain.ItemGroup;
import com.application.eurder2.domain.User;
import com.application.eurder2.dtos.request.CreateItemGroupRequest;
import com.application.eurder2.dtos.response.EurderResponse;
import com.application.eurder2.exception.ExistenceException;
import com.application.eurder2.repository.EurderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Unit tests for EurderService.reeurderAnExistingEurder. */
class EurderServiceReEurderTest {

    private EurderRepository repository;
    private ItemGroupService itemGroupService;
    private EurderService service;
    private User user;
    private UUID eurderId;

    @BeforeEach
    void setUp() {
        repository = mock(EurderRepository.class);
        itemGroupService = mock(ItemGroupService.class);
        service = new EurderService(repository, itemGroupService);
        user = mock(User.class);
        when(user.getId()).thenReturn(UUID.randomUUID());
        eurderId = UUID.randomUUID();
        when(repository.save(any(Eurder.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Item itemWithId(UUID id, String name, double price) {
        Item item = mock(Item.class);
        when(item.getId()).thenReturn(id);
        when(item.getName()).thenReturn(name);
        when(item.getPrice()).thenReturn(price);
        when(item.hasSufficientStockFor(anyInt())).thenReturn(true);
        return item;
    }

    @Test
    void unknownOrNotOwnEurder_throwsExistenceException_andSavesNothing() {
        when(repository.findByIdAndUser(eurderId, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reeurderAnExistingEurder(eurderId, user))
                .isInstanceOf(ExistenceException.class)
                .hasMessage("This eurder is not found in the database.");

        verify(repository).findByIdAndUser(eurderId, user);
        verify(repository, never()).save(any());
        verifyNoInteractions(itemGroupService);
    }

    @Test
    void createsItemGroupForEveryGroupOfOriginal_withSameItemIdAndAmount() {
        UUID penId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        Item pen = itemWithId(penId, "Pen", 2.5);
        Item book = itemWithId(bookId, "Book", 15.0);
        Eurder original = new Eurder(List.of(new ItemGroup(pen, 2), new ItemGroup(book, 1)), user);
        when(repository.findByIdAndUser(eurderId, user)).thenReturn(Optional.of(original));
        when(itemGroupService.createItemGroup(any())).thenAnswer(inv -> new ItemGroup(pen, 1));

        service.reeurderAnExistingEurder(eurderId, user);

        ArgumentCaptor<CreateItemGroupRequest> captor = ArgumentCaptor.forClass(CreateItemGroupRequest.class);
        verify(itemGroupService, times(2)).createItemGroup(captor.capture());
        assertThat(captor.getAllValues()).containsExactly(
                new CreateItemGroupRequest(penId, 2),
                new CreateItemGroupRequest(bookId, 1));
    }

    @Test
    void savesNewEurderForSameUser_andReturnsIt() {
        Item pen = itemWithId(UUID.randomUUID(), "Pen", 2.5);
        Eurder original = new Eurder(List.of(new ItemGroup(pen, 2)), user);
        when(repository.findByIdAndUser(eurderId, user)).thenReturn(Optional.of(original));
        when(itemGroupService.createItemGroup(any())).thenAnswer(inv -> new ItemGroup(pen, 2));

        EurderResponse response = service.reeurderAnExistingEurder(eurderId, user);

        ArgumentCaptor<Eurder> captor = ArgumentCaptor.forClass(Eurder.class);
        verify(repository).save(captor.capture());
        Eurder saved = captor.getValue();
        assertThat(saved).isNotSameAs(original);
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getItemGroups()).hasSize(1);
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.eurderPrice()).isEqualTo(5.0);
    }

    @Test
    void usesCurrentItemPrice_notPriceOfOriginal() {
        Item pen = itemWithId(UUID.randomUUID(), "Pen", 2.5);
        Eurder original = new Eurder(List.of(new ItemGroup(pen, 2)), user); // 2 x 2.5 = 5.0
        when(pen.getPrice()).thenReturn(100.0);                             // price changed since
        when(repository.findByIdAndUser(eurderId, user)).thenReturn(Optional.of(original));
        when(itemGroupService.createItemGroup(any())).thenAnswer(inv -> new ItemGroup(pen, 2));

        EurderResponse response = service.reeurderAnExistingEurder(eurderId, user);

        assertThat(original.getTotalPrice()).isEqualTo(5.0);
        assertThat(response.eurderPrice()).isEqualTo(200.0);
    }
}