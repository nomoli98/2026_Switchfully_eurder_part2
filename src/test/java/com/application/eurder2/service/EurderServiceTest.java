package com.application.eurder2.service;

import com.application.eurder2.domain.Eurder;
import com.application.eurder2.domain.Item;
import com.application.eurder2.domain.ItemGroup;
import com.application.eurder2.domain.User;
import com.application.eurder2.dtos.request.CreateEurderRequest;
import com.application.eurder2.dtos.request.CreateItemGroupRequest;
import com.application.eurder2.dtos.response.EurderResponse;
import com.application.eurder2.exception.ExistenceException;
import com.application.eurder2.repository.EurderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EurderServiceTest {

    private EurderRepository repository;
    private ItemGroupService itemGroupService;
    private EurderService service;
    private User user;

    private final CreateItemGroupRequest penRequest = new CreateItemGroupRequest(UUID.randomUUID(), 4);
    private final CreateItemGroupRequest bookRequest = new CreateItemGroupRequest(UUID.randomUUID(), 2);
    private final ItemGroup pen = new ItemGroup(new Item("Pen", "Blue pen", 2.5, 10), 4);
    private final ItemGroup book = new ItemGroup(new Item("Book", "Thick book", 15.0, 10), 2);

    @BeforeEach
    void setUp() {
        repository = mock(EurderRepository.class);
        itemGroupService = mock(ItemGroupService.class);
        service = new EurderService(repository, itemGroupService);
        user = mock(User.class);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(itemGroupService.createItemGroup(penRequest)).thenReturn(pen);
        when(itemGroupService.createItemGroup(bookRequest)).thenReturn(book);
        when(repository.save(any(Eurder.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void createEurder_savesEurderWithUserAndGroupsInOrder() {
        service.createEurder(new CreateEurderRequest(List.of(penRequest, bookRequest)), user);

        ArgumentCaptor<Eurder> captor = ArgumentCaptor.forClass(Eurder.class);
        verify(repository, times(1)).save(captor.capture());
        Eurder saved = captor.getValue();
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getItemGroups()).containsExactly(pen, book);
    }

    @Test
    void createEurder_returnsResponseOfSavedEurder() {
        EurderResponse response =
                service.createEurder(new CreateEurderRequest(List.of(penRequest, bookRequest)), user);

        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.eurderPrice()).isEqualTo(40.0);
        assertThat(response.items()).hasSize(2);
    }

    @Test
    void createEurder_unknownItem_savesNothing() {
        CreateItemGroupRequest unknown = new CreateItemGroupRequest(UUID.randomUUID(), 1);
        when(itemGroupService.createItemGroup(unknown)).thenThrow(ExistenceException.notInDatabase("item"));

        assertThatThrownBy(() -> service.createEurder(
                new CreateEurderRequest(List.of(penRequest, unknown)), user))
                .isInstanceOf(ExistenceException.class);
        verify(repository, never()).save(any());
    }
}