package com.ticket_online.domain.catalog.application;

import com.ticket_online.domain.catalog.dao.SeatRepository;
import com.ticket_online.domain.catalog.dao.ShowRepository;
import com.ticket_online.domain.catalog.domain.Seat;
import com.ticket_online.domain.catalog.domain.Show;
import com.ticket_online.domain.catalog.dto.request.CreateShowRequest;
import com.ticket_online.domain.catalog.dto.response.CreateShowResponse;
import com.ticket_online.domain.catalog.dto.response.FindShowResponse;
import com.ticket_online.domain.catalog.dto.response.SeatResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShowService {
    private final ShowRepository showRepository;
    private final SeatService seatService;
    private final SeatRepository seatRepository;

    @Transactional(readOnly = true)
    public List<FindShowResponse> findAllShow() {
        List<Show> shows = showRepository.findAll();
        return shows.stream()
                .map(
                        show ->
                                FindShowResponse.of(
                                        show.getId(),
                                        show.getName(),
                                        show.getStart_time(),
                                        show.getLocation()))
                .toList();
    }

    public CreateShowResponse createShow(CreateShowRequest request) {
        Show show = Show.createShow(request.startTime(), request.name(), request.location());
        Show savedShow = showRepository.save(show);
        // SeatType: VIP / NORMAL / ECONOMY
        seatService.createSeatsForShow(savedShow.getId(), request.totalSeats(), request.price());
        return CreateShowResponse.from(savedShow);
    }

    public List<SeatResponse> findSeatsByShow(Long showId) {

        List<Seat> seats = seatRepository.findByShowId(showId);

        return seats.stream().map(SeatResponse::from).toList();
    }
}
