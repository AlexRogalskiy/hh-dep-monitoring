package ru.hh.school.depmonitoring.rs;

import org.junit.Before;
import org.junit.Test;
import ru.hh.school.depmonitoring.DepMonitoringTestBase;
import ru.hh.school.depmonitoring.dto.PageDto;
import ru.hh.school.depmonitoring.dto.RepositoryDto;
import ru.hh.school.depmonitoring.utils.DBUtils;
import ru.hh.school.depmonitoring.utils.StructCreator;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RepositoryResourceTest extends DepMonitoringTestBase {
    @Inject
    private DBUtils dbUtils;

    @Before
    public void fillTable() {
        dbUtils.fillRepositoryTable();
    }

    @Test
    public void getItemByIdTest() {
        RepositoryDto repositoryDto = target("/repository/1")
                .request()
                .get(RepositoryDto.class);
        assertRepositoryDtoIsEquals(StructCreator.createRepositoryDto(), repositoryDto);
    }

    @Test
    public void getNonExistingItemTest() {
        Response response = this.createRequest("/repository/99").get();
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void getFirstPageTest() {
        PageDto<RepositoryDto> controlPage = PageDto.<RepositoryDto>builder()
                .withItems(StructCreator.createRepositoryDtoList().subList(0, 5))
                .withFound(10)
                .withPage(0)
                .withPerPage(5)
                .withPages(2)
                .build();

        PageDto<RepositoryDto> resultPage = getPageRequestContent(0, 5);
        assertRepositoryPageDtoIsEquals(controlPage, resultPage);
    }

    @Test
    public void getLastPageTest() {
        PageDto<RepositoryDto> controlPage = PageDto.<RepositoryDto>builder()
                .withItems(StructCreator.createRepositoryDtoList().subList(8, 10))
                .withFound(10)
                .withPage(2)
                .withPerPage(4)
                .withPages(3)
                .build();

        PageDto<RepositoryDto> resultPage = getPageRequestContent(2, 4);
        assertRepositoryPageDtoIsEquals(controlPage, resultPage);
    }

    @Test
    public void getInvalidPageTest() {
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), getPageRequestStatus(0, 0));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), getPageRequestStatus(0, -1));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), getPageRequestStatus(-1, 4));
    }


    @Test
    public void getNonExistingPageTest() {
        PageDto<RepositoryDto> controlPage = PageDto.<RepositoryDto>builder()
                .withItems(new ArrayList<>())
                .withFound(10)
                .withPage(100)
                .withPages(5)
                .withPerPage(2)
                .build();

        PageDto<RepositoryDto> resultPage = getPageRequestContent(100, 2);
        assertRepositoryPageDtoIsEquals(controlPage, resultPage);
    }

    @Test
    public void putItemWithIdTest() {
        dbUtils.addItemToRepositoryTable(9999L);
        RepositoryDto dto = StructCreator.createRepositoryDto();
        dto.setRepositoryId(9999L);
        Response response = target("/repository/9999")
                .request()
                .put(Entity.json(dto));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void putItemWrongIdTest() {
        RepositoryDto dto = StructCreator.createRepositoryDto();
        Response response = target("/repository/2")
                .request()
                .put(Entity.json(dto));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    public static void assertRepositoryDtoIsEquals(RepositoryDto dto1, RepositoryDto dto2) {
        assertEquals(dto1, dto2);
        assertNotNull(dto1);
        assertNotNull(dto2);
        assertEquals(dto1.getRepositoryId(), dto2.getRepositoryId());
        assertEquals(dto1.getHtmlUrl(), dto2.getHtmlUrl());
        assertEquals(dto1.getDescription(), dto2.getDescription());
        assertEquals(dto1.isArchived(), dto2.isArchived());
        assertEquals(dto1.isActive(), dto2.isActive());
        assertEquals(dto1.getCreatedAt(), dto2.getCreatedAt());
        assertEquals(dto1.getUpdatedAt(), dto2.getUpdatedAt());
    }

    public static void assertRepositoryPageDtoIsEquals(PageDto<RepositoryDto> page1, PageDto<RepositoryDto> page2) {
        assertNotNull(page1);
        assertNotNull(page2);
        assertNotNull(page1.getItems());
        assertNotNull(page2.getItems());
        assertEquals(page1.getItems().size(), page2.getItems().size());
        assertRepositoryDtoListIsEqual(page1.getItems(), page2.getItems());
        assertEquals(page1.getFound(), page2.getFound());
        assertEquals(page1.getPage(), page2.getPage());
        assertEquals(page1.getPerPage(), page2.getPerPage());
        assertEquals(page1.getPages(), page2.getPages());
    }

    public static void assertRepositoryDtoListIsEqual(List<RepositoryDto> list1, List<RepositoryDto> list2) {
        assertNotNull(list1);
        assertNotNull(list2);
        assertEquals(list1.size(), list2.size());
        for (int i = 0; i < list1.size(); i++) {
            assertRepositoryDtoIsEquals(list1.get(i), list2.get(i));
        }
    }

    public PageDto<RepositoryDto> getPageRequestContent(int page, int perPage) {
        return target("/repository/page")
                .queryParam("page", page)
                .queryParam("perPage", perPage)
                .request()
                .get()
                .readEntity(new GenericType<PageDto<RepositoryDto>>() {
                });
    }

    public int getPageRequestStatus(int page, int perPage) {
        return target("/repository/page")
                .queryParam("page", page)
                .queryParam("perPage", perPage)
                .request()
                .get().getStatus();
    }
}
