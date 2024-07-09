package ${project_rootPackage}.service;

import io.jmix.data.Sequence;
import io.jmix.data.Sequences;
import org.springframework.stereotype.Service;

@Service("${normalizedPrefix_underscore}InvoiceService")
public class InvoiceServiceImpl implements InvoiceService {

    private final Sequences sequences;

    public InvoiceServiceImpl(Sequences sequences) {
        this.sequences = sequences;
    }

    public long getNextValue() {
        Sequence sequence = Sequence.withName("invoice_number_seq")
                .setStartValue(0)
                .setIncrement(1);

        return sequences.createNextValue(sequence);
    }

}